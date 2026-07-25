import { Component, OnInit } from '@angular/core';
import { MonitoringService, AnalyticsDto, HistoryPointDto, MetricPointDto, MetricSummaryDto, TimeRange } from '../core/services/monitoring.service';

interface ChartDef {
  points: string;
  current: number;
  min: number;
  max: number;
  dates: { x: number; label: string }[];
}

interface BarDef {
  bars: { x: number; w: number; y: number; h: number; value: number }[];
  current: number;
  maxLabel: string;
}

interface GaugeDef {
  arc: string; bg: string;
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
  summary: MetricSummaryDto | null = null;

  range: TimeRange = '7d';
  ranges = [
    { key: '1h' as TimeRange, label: '1h' },
    { key: '6h' as TimeRange, label: '6h' },
    { key: '24h' as TimeRange, label: '24h' },
    { key: '7d' as TimeRange, label: '7j' },
    { key: '30d' as TimeRange, label: '30j' },
  ];

  uptime?: ChartDef;
  cpu?: ChartDef;
  ram?: BarDef;
  storage?: GaugeDef;

  palette = { uptime: '#10b981', cpu: '#3b82f6', ram: '#8b5cf6', storage: '#f59e0b' };

  constructor(private api: MonitoringService) {}

  ngOnInit() { this.load(); }

  load() {
    this.loading = true;
    this.api.getHistory().subscribe({
      next: d => { this.data = d; this.build(d); this.loading = false; },
      error: () => { this.error = 'Erreur de chargement.'; this.loading = false; }
    });
    const { from, to } = this.api.getTimeRangeDates(this.range);
    this.api.getMetricsSummary(from, to).subscribe({ next: s => this.summary = s });
  }

  onRangeChange() { this.load(); }

  exportCsv() {
    const { from, to } = this.api.getTimeRangeDates(this.range);
    this.api.exportMetricsCsv(from, to).subscribe({
      next: blob => {
        const a = document.createElement('a');
        a.href = URL.createObjectURL(blob);
        a.download = `medilink-metrics-${from.slice(0,10)}.csv`;
        a.click();
      }
    });
  }

  barColor(v: number) { return v > 80 ? '#ef4444' : v > 60 ? '#f59e0b' : v > 40 ? '#8b5cf6' : '#22c55e'; }
  gaugeColor(v: number) { return v > 80 ? '#ef4444' : v > 60 ? '#f59e0b' : v > 40 ? '#eab308' : '#10b981'; }

  private build(d: AnalyticsDto) {
    this.uptime = this.line(d.uptime30Days, 99, 100);
    this.cpu = this.line(d.cpuEvolution);
    this.ram = this.bars(d.ramEvolution);
    this.storage = this.gauge(d.storageEvolution);
  }

  private line(data: HistoryPointDto[], mn?: number, mx?: number): ChartDef {
    if (!data || data.length === 0) return { points: '', current: 0, min: 0, max: 0, dates: [] };
    const vals = data.map(d => d.value);
    const yMin = mn ?? Math.min(...vals);
    const yMax = mx ?? Math.max(...vals);
    const yR = Math.max(yMax - yMin, 1);
    const n = data.length;
    const W = 180, H = 26;
    const padL = 0, padR = 0, padT = 1;
    const cw = W - padL - padR;
    const ch = H - padT;
    const pts = data.map((p, i) => {
      const x = padL + (n > 1 ? (i / (n - 1)) : 0.5) * cw;
      const y = padT + ch - ((p.value - yMin) / yR) * ch;
      return `${x.toFixed(2)},${y.toFixed(2)}`;
    });
    const fmt = (r: string) => {
      try { const dt = new Date(r); if (!isNaN(+dt)) return `${String(dt.getDate()).padStart(2,'0')}/${String(dt.getMonth()+1).padStart(2,'0')}`; } catch {}
      return r.slice(0,5);
    };
    const idx = n <= 3 ? data.map((_, i) => i) : [0, Math.floor((n-1)/3), Math.floor(2*(n-1)/3), n-1];
    return {
      points: pts.join(' '),
      current: vals[vals.length - 1] ?? 0,
      min: Math.min(...vals),
      max: Math.max(...vals),
      dates: idx.map(i => ({ x: padL + (n > 1 ? (i / (n - 1)) : 0.5) * cw, label: fmt(data[i]?.date ?? '') }))
    };
  }

  private bars(data: HistoryPointDto[]): BarDef {
    if (!data || data.length === 0) return { bars: [], current: 0, maxLabel: '' };
    const vals = data.map(d => d.value);
    const mx = Math.max(...vals) * 1.15 || 100;
    const yR = mx || 1;
    const n = data.length;
    const W = 180, H = 28;
    const padL = 0, padT = 1;
    const cw = W - padL;
    const ch = H - padT;
    const gap = 1.5;
    const bw = Math.max(2, Math.min(8, (cw - gap * (n - 1)) / n));
    const tw = n * bw + (n - 1) * gap;
    const off = padL + (cw - tw) / 2;
    return {
      bars: data.map((p, i) => {
        const x = off + i * (bw + gap);
        const bh = ((p.value - 0) / yR) * ch;
        return { x, w: bw, y: padT + ch - bh, h: bh, value: p.value };
      }),
      current: vals[vals.length - 1] ?? 0,
      maxLabel: Math.round(mx).toString()
    };
  }

  private gauge(data: HistoryPointDto[]): GaugeDef {
    const v = data?.length ? data[data.length - 1].value : 0;
    const cx = 50, cy = 38, r = 16, c = this.gaugeColor(v);
    const bg = `M ${cx - r} ${cy} A ${r} ${r} 0 0 0 ${cx + r} ${cy}`;
    const a = (180 - (v / 100) * 180) * Math.PI / 180;
    const arc = `M ${cx - r} ${cy} A ${r} ${r} 0 0 0 ${(cx + r * Math.cos(a)).toFixed(1)} ${(cy - r * Math.sin(a)).toFixed(1)}`;
    return { arc, bg, value: v, color: c };
  }
}