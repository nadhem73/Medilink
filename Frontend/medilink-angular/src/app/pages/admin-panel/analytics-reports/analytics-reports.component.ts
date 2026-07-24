import { Component, OnInit } from '@angular/core';
import { MonitoringService, AnalyticsDto, HistoryPointDto, MetricPointDto, MetricSummaryDto, TimeRange } from '../core/services/monitoring.service';

interface ChartMeta {
  points: string;
  fillPoints: string;
  lineYMax: number;
  lineYMin: number;
  lineCurrent: number;
  dateLabels: { x: number; label: string }[];
  yMaxLabel: string;
  yMinLabel: string;
}

interface BarDatum {
  x: number; w: number; y: number; h: number;
  value: number; label: string;
}

interface BarChartMeta {
  bars: BarDatum[];
  yMaxLabel: string;
  lineCurrent: number;
}

interface GaugeMeta {
  arcPath: string; bgArcPath: string;
  value: number; color: string;
}

@Component({
  selector: 'app-analytics-reports',
  templateUrl: './analytics-reports.component.html',
  styleUrls: ['./analytics-reports.component.scss']
})
export class AnalyticsReportsComponent implements OnInit {
  data: AnalyticsDto | null = null;
  loading = true;
  error = '';

  totalIncidents = 0;
  summary: MetricSummaryDto | null = null;

  selectedTimeRange: TimeRange = '7d';
  timeRanges: { key: TimeRange; label: string }[] = [
    { key: '1h', label: '1h' }, { key: '6h', label: '6h' },
    { key: '24h', label: '24h' }, { key: '7d', label: '7j' }, { key: '30d', label: '30j' },
  ];

  overlayMetric: string | null = null;
  overlayData: MetricPointDto[] = [];

  uptimeChart?: ChartMeta;
  cpuChart?: ChartMeta;
  ramBars?: BarChartMeta;
  storageGauge?: GaugeMeta;

  colors = { uptime: '#10b981', cpu: '#3b82f6', ram: '#8b5cf6', storage: '#f59e0b' };

  constructor(private monitoring: MonitoringService) {}

  ngOnInit(): void { this.loadData(); }

  loadData(): void {
    this.loading = true;
    this.monitoring.getHistory().subscribe({
      next: (d) => {
        this.data = d;
        this.totalIncidents = d.incidents?.length ?? 0;
        this.buildCharts(d);
        this.loading = false;
      },
      error: () => { this.error = 'Impossible de charger les données.'; this.loading = false; }
    });
    this.loadSummary();
  }

  onTimeRangeChange(): void { this.loadData(); }

  loadSummary(): void {
    const { from, to } = this.monitoring.getTimeRangeDates(this.selectedTimeRange);
    this.monitoring.getMetricsSummary(from, to).subscribe({ next: (s) => { this.summary = s; } });
  }

  toggleOverlay(metric: string): void {
    if (this.overlayMetric === metric) { this.overlayMetric = null; this.overlayData = []; return; }
    this.overlayMetric = metric;
    const { from, to } = this.monitoring.getTimeRangeDates(this.selectedTimeRange);
    this.monitoring.getMetricsRange(undefined, metric, from, to).subscribe({ next: (d) => { this.overlayData = d; } });
  }

  getOverlayPoints(): string {
    if (this.overlayData.length < 2) return '';
    const values = this.overlayData.map(d => d.value);
    const min = Math.min(...values);
    const max = Math.max(...values);
    const range = max - min || 1;
    const padL = 18, padT = 5, bottom = 30;
    const chartW = 130 - 18 - 4;
    const chartH = bottom - padT;
    return this.overlayData.map((d, i) => {
      const x = padL + (i / (this.overlayData.length - 1)) * chartW;
      const y = padT + chartH - ((d.value - min) / range) * chartH;
      return `${x.toFixed(1)},${y.toFixed(1)}`;
    }).join(' ');
  }

  exportCsv(): void {
    const { from, to } = this.monitoring.getTimeRangeDates(this.selectedTimeRange);
    this.monitoring.exportMetricsCsv(from, to).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url; a.download = `metrics-${from.substring(0, 10)}-${to.substring(0, 10)}.csv`;
        a.click(); window.URL.revokeObjectURL(url);
      }
    });
  }

  barColor(v: number): string {
    return v > 80 ? '#ef4444' : v > 60 ? '#f59e0b' : v > 40 ? '#8b5cf6' : '#22c55e';
  }

  gaugeColor(v: number): string {
    return v > 80 ? '#ef4444' : v > 60 ? '#f59e0b' : v > 40 ? '#eab308' : '#10b981';
  }

  private buildCharts(d: AnalyticsDto): void {
    this.uptimeChart = this.buildLine(d.uptime30Days, this.colors.uptime, 99, 100);
    this.cpuChart = this.buildLine(d.cpuEvolution, this.colors.cpu);
    this.ramBars = this.buildBars(d.ramEvolution);
    this.storageGauge = this.buildGauge(d.storageEvolution);
  }

  private buildLine(data: HistoryPointDto[], color: string, fixedMin?: number, fixedMax?: number): ChartMeta {
    if (!data || data.length === 0) {
      return { points: '', fillPoints: '', lineYMax: 0, lineYMin: 0, lineCurrent: 0, dateLabels: [], yMaxLabel: '', yMinLabel: '' };
    }
    const values = data.map(d => d.value);
    let yMin = fixedMin ?? Math.min(...values);
    let yMax = fixedMax ?? Math.max(...values);
    if (yMin === yMax) yMax = yMin + 1;
    const yRange = yMax - yMin;
    const n = data.length;
    const padL = 18, padR = 4, padT = 5, bottom = 30;
    const chartW = 130 - padL - padR;
    const chartH = bottom - padT;

    const pts: string[] = [];
    data.forEach((p, i) => {
      const x = padL + (n > 1 ? (i / (n - 1)) : 0.5) * chartW;
      const y = padT + chartH - ((p.value - yMin) / yRange) * chartH;
      pts.push(`${x.toFixed(2)},${y.toFixed(2)}`);
    });

    const firstX = padL;
    const lastX = padL + chartW;
    const fillCoords = [`${firstX.toFixed(2)},${bottom}`, ...pts, `${lastX.toFixed(2)},${bottom}`];

    const fmtDate = (raw: string) => {
      if (!raw) return '';
      try {
        const dt = new Date(raw);
        if (!isNaN(dt.getTime())) return `${String(dt.getDate()).padStart(2,'0')}/${String(dt.getMonth()+1).padStart(2,'0')}`;
      } catch { }
      return raw.substring(0, 5);
    };

    const fmtVal = (v: number) => fixedMin !== undefined ? `${v.toFixed(1)}%` : Math.round(v).toString();

    const labelIndices = n <= 3 ? data.map((_, i) => i) : [0, Math.floor(n/3), Math.floor(2*n/3), n-1];
    const dateLabels = labelIndices.map(i => ({
      x: padL + (n > 1 ? (i / (n - 1)) : 0.5) * chartW,
      label: fmtDate(data[i]?.date ?? '')
    }));

    return {
      points: pts.join(' '),
      fillPoints: fillCoords.join(' '),
      lineYMax: Math.max(...values),
      lineYMin: Math.min(...values),
      lineCurrent: values[values.length - 1] ?? 0,
      dateLabels,
      yMaxLabel: fmtVal(yMax),
      yMinLabel: fmtVal(yMin)
    };
  }

  private buildBars(data: HistoryPointDto[]): BarChartMeta {
    if (!data || data.length === 0) return { bars: [], yMaxLabel: '', lineCurrent: 0 };
    const values = data.map(d => d.value);
    const yMin = 0;
    const yMax = Math.max(...values) * 1.15 || 100;
    const yRange = yMax - yMin || 1;
    const n = data.length;
    const padL = 12, padR = 4, padT = 5, bottom = 34;
    const chartW = 130 - padL - padR;
    const chartH = bottom - padT;
    const barGap = 2;
    const barW = Math.max(3, Math.min(12, (chartW - barGap * (n - 1)) / n));
    const totalW = n * barW + (n - 1) * barGap;
    const offsetX = padL + (chartW - totalW) / 2;

    const bars: BarDatum[] = data.map((p, i) => {
      const x = offsetX + i * (barW + barGap);
      const barH = ((p.value - yMin) / yRange) * chartH;
      const y = padT + chartH - barH;
      return { x, w: barW, y, h: barH, value: p.value, label: '' };
    });

    return { bars, yMaxLabel: Math.round(yMax).toString(), lineCurrent: values[values.length - 1] ?? 0 };
  }

  private buildGauge(data: HistoryPointDto[]): GaugeMeta {
    const cx = 50, cy = 34, r = 15;
    const value = data && data.length > 0 ? data[data.length - 1].value : 0;
    const bgArcPath = `M ${cx - r} ${cy} A ${r} ${r} 0 0 0 ${cx + r} ${cy}`;
    const arcPath = this.gaugeArc(cx, cy, r, value);
    return { arcPath, bgArcPath, value, color: this.gaugeColor(value) };
  }

  private gaugeArc(cx: number, cy: number, r: number, percent: number): string {
    const endAngle = 180 - (percent / 100) * 180;
    const endRad = endAngle * Math.PI / 180;
    const x1 = cx - r;
    const y1 = cy;
    const x2 = cx + r * Math.cos(endRad);
    const y2 = cy - r * Math.sin(endRad);
    return `M ${x1.toFixed(1)} ${y1.toFixed(1)} A ${r} ${r} 0 0 0 ${x2.toFixed(1)} ${y2.toFixed(1)}`;
  }

  severityClass(s: string): string {
    switch ((s || '').toLowerCase()) {
      case 'critical': return 'sev-critical';
      case 'high': return 'sev-high';
      case 'medium': return 'sev-medium';
      case 'low': return 'sev-low';
      default: return 'sev-low';
    }
  }

  formatDateLong(raw: string): string {
    if (!raw) return '';
    try {
      const dt = new Date(raw);
      if (!isNaN(dt.getTime())) return dt.toLocaleDateString('fr-FR', { day: '2-digit', month: 'short', year: 'numeric' });
    } catch { }
    return raw;
  }
}