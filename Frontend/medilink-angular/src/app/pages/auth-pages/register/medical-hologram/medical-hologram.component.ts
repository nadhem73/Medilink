import {
  AfterViewInit,
  Component,
  ElementRef,
  Input,
  NgZone,
  OnChanges,
  OnDestroy,
  SimpleChanges,
  ViewChild
} from '@angular/core';
import * as THREE from 'three';
import { GLTFLoader } from 'three/examples/jsm/loaders/GLTFLoader.js';
import { DRACOLoader } from 'three/examples/jsm/loaders/DRACOLoader.js';
import { gsap } from 'gsap';

/**
 * Hologramme médical 3D — « digital twin » rendu avec Three.js.
 *
 * Couche PRÉSENTATION uniquement : reçoit des données déjà validées du
 * formulaire d'inscription et les visualise. Aucune logique métier ici.
 *
 *  - Charge un MODÈLE HUMAIN RÉALISTE (.glb : male.glb / female.glb) et lui
 *    applique un traitement hologramme (contour Fresnel lumineux, lignes de
 *    scan, maillage). C'est le vrai mesh humain qui donne le réalisme.
 *  - Si aucun .glb n'est présent dans assets/models/holo/, un corps filaire
 *    de secours (procédural) s'affiche automatiquement.
 *  - Morphing temps réel : genre (modèle dédié), taille (élongation),
 *    poids (corpulence) — interpolés avec GSAP.
 *  - Système circulatoire : particules + cœur pulsé au choix du groupe sanguin.
 *  - Balayage de scan médical intensifié pendant la séquence finale.
 */
@Component({
  selector: 'app-medical-hologram',
  standalone: true,
  template: `<div class="holo-gl" #host></div>`,
  styles: [`
    :host { display:block; width:100%; height:100%; }
    .holo-gl { width:100%; height:100%; }
    .holo-gl canvas { display:block; width:100% !important; height:100% !important; }
  `]
})
export class MedicalHologramComponent implements AfterViewInit, OnChanges, OnDestroy {
  @ViewChild('host', { static: true }) host!: ElementRef<HTMLDivElement>;

  @Input() gender = ''; // 'MALE' | 'FEMALE' | 'OTHER' | ''
  @Input() heightCm: number | null = null;
  @Input() weightKg: number | null = null;
  @Input() bloodGroup = '';
  @Input() scanning = false;

  // --- Three.js core ---
  private renderer!: THREE.WebGLRenderer;
  private scene!: THREE.Scene;
  private camera!: THREE.PerspectiveCamera;
  private clock = new THREE.Clock();
  private raf = 0;
  private ro?: ResizeObserver;
  private reduced = false;

  // --- Rig ---
  private root!: THREE.Group;          // rotation + échelle taille/poids
  private modelGroup!: THREE.Group;    // accueille le modèle GLB réaliste
  private fallback!: THREE.Group;      // corps procédural de secours (si pas de .glb)
  private upper!: THREE.Group;         // (fallback) carrure
  private lower!: THREE.Group;         // (fallback) bassin
  private fillMat!: THREE.ShaderMaterial;   // intérieur sombre translucide
  private edgeMat!: THREE.ShaderMaterial;   // arêtes filaires lumineuses
  private nodeMat!: THREE.ShaderMaterial;   // nœuds brillants aux sommets
  private surfMat!: THREE.ShaderMaterial;   // surface hologramme du modèle réel
  private shaderMats: THREE.ShaderMaterial[] = []; // pour màj uTime/uScanY/uBoost
  private heart!: THREE.Mesh;
  private blood!: THREE.Points;
  private bloodMat!: THREE.ShaderMaterial;
  private bloodVel!: Float32Array;       // vitesse de chaque globule le long de son vaisseau
  private bloodT!: Float32Array;         // position param. courante (0..1) sur le vaisseau
  private bloodCurveIdx!: Int16Array;    // index du vaisseau emprunté
  private bloodCurves: THREE.CatmullRomCurve3[] = []; // réseau vasculaire (boucles fermées)
  private vessels!: THREE.Group;         // lignes de vaisseaux lumineuses
  private vesselMats: THREE.LineBasicMaterial[] = [];
  private vesselLines: THREE.Line[] = []; // pour recâbler la géométrie selon le modèle
  private bloodBounds = { y0: 0.05, y1: 1.7, r: 0.2 }; // bornes verticales (balayage de scan)
  // proportions du corps actuellement affiché (adaptent le circuit au modèle réel)
  private static readonly FALLBACK_DIMS = { H: 1.8, W: 1.8 * 0.17, D: 1.8 * 0.11, cx: 0, cz: 0 };
  private bloodDims = MedicalHologramComponent.FALLBACK_DIMS;
  private static readonly VESSEL_SEGMENTS = 120;

  // --- Chargement des modèles GLB ---
  private loader!: GLTFLoader;
  private modelCache: Record<string, THREE.Group> = {};
  private loadedGender = '';            // genre du modèle actuellement affiché
  private usingFallback = true;         // true tant qu'aucun .glb réel n'est chargé
  private static readonly MODEL_PATHS: Record<string, string> = {
    MALE:   '/assets/models/holo/male.glb',
    FEMALE: '/assets/models/holo/female.glb',
    OTHER:  '/assets/models/holo/male.glb'
  };

  // état animé (proxies tweenés par GSAP)
  private anim = { girth: 1, scanY: 1.9, bloodOn: 0, scanBoost: 0 };
  private pointer = { x: 0, y: 0, tx: 0, ty: 0 };

  constructor(private zone: NgZone) {}

  ngAfterViewInit(): void {
    this.reduced = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
    this.initThree();
    this.initLoader();
    this.buildMaterials();
    this.buildFallbackHuman();
    this.buildCirculation();
    this.applyGender(true);
    this.applyMorph(true);
    this.applyBlood(true);

    this.zone.runOutsideAngular(() => {
      this.animate();
    });

    this.ro = new ResizeObserver(() => this.resize());
    this.ro.observe(this.host.nativeElement);
    this.host.nativeElement.addEventListener('pointermove', this.onPointer);
  }

  ngOnChanges(c: SimpleChanges): void {
    if (!this.renderer) return; // pas encore initialisé
    if (c['gender']) this.applyGender();
    if (c['heightCm'] || c['weightKg']) this.applyMorph();
    if (c['bloodGroup']) this.applyBlood();
    if (c['scanning']) {
      gsap.to(this.anim, { scanBoost: this.scanning ? 1 : 0, duration: 0.8, ease: 'power2.out' });
    }
  }

  ngOnDestroy(): void {
    cancelAnimationFrame(this.raf);
    this.ro?.disconnect();
    this.host?.nativeElement?.removeEventListener('pointermove', this.onPointer);
    gsap.killTweensOf([this.anim, this.pointer]);
    this.scene?.traverse(o => {
      const m = o as THREE.Mesh;
      if (m.geometry) m.geometry.dispose();
      const mat = (m as any).material;
      if (Array.isArray(mat)) mat.forEach((x: THREE.Material) => x.dispose());
      else if (mat?.dispose) mat.dispose();
    });
    this.renderer?.dispose();
  }

  // =====================================================================
  //  INIT
  // =====================================================================
  private initThree(): void {
    const el = this.host.nativeElement;
    const w = el.clientWidth || 360;
    const h = el.clientHeight || 480;

    this.renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true, powerPreference: 'high-performance' });
    this.renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));
    this.renderer.setSize(w, h);
    this.renderer.setClearColor(0x000000, 0);
    el.appendChild(this.renderer.domElement);

    this.scene = new THREE.Scene();

    // éclairage doux (utile si un modèle GLB conserve une part de matériau standard)
    this.scene.add(new THREE.AmbientLight(0x4080ff, 0.6));
    const key = new THREE.DirectionalLight(0x9fd8ff, 0.8);
    key.position.set(2, 4, 3);
    this.scene.add(key);

    // hiérarchie : root (rotation/échelle) -> modelGroup (GLB) + fallback (procédural)
    this.root = new THREE.Group();
    this.modelGroup = new THREE.Group();
    this.fallback = new THREE.Group();
    this.root.add(this.modelGroup, this.fallback);
    this.scene.add(this.root);

    this.camera = new THREE.PerspectiveCamera(34, w / h, 0.1, 100);
    this.camera.position.set(0, 1.02, 4.05);
    this.camera.lookAt(0, 0.95, 0);

    // halo ambiant doux derrière le sujet (fond BLANC → teinte bleutée légère, blending normal)
    const halo = new THREE.Mesh(
      new THREE.CircleGeometry(2.4, 48),
      new THREE.MeshBasicMaterial({
        color: 0xbfe0f2, transparent: true, opacity: 0.30,
        blending: THREE.NormalBlending, depthWrite: false
      })
    );
    halo.position.set(0, 0.95, -1.6);
    this.scene.add(halo);

    // anneaux de socle holographique (visibles sur blanc → teal soutenu, blending normal)
    for (let i = 0; i < 3; i++) {
      const ring = new THREE.Mesh(
        new THREE.RingGeometry(0.5 + i * 0.22, 0.515 + i * 0.22, 80),
        new THREE.MeshBasicMaterial({
          color: 0x0d8aa0, transparent: true, opacity: 0.34 - i * 0.09,
          blending: THREE.NormalBlending, depthWrite: false, side: THREE.DoubleSide
        })
      );
      ring.rotation.x = -Math.PI / 2;
      ring.position.y = 0.02;
      this.scene.add(ring);
    }
  }

  // =====================================================================
  //  SHADERS — style « réseau filaire low-poly » (réf : hologramme exemple)
  // =====================================================================

  /** Code GLSL commun : balayage de scan + couleur animée. */
  private static readonly COMMON_UNIFORMS = `
    uniform float uTime;
    uniform vec3  uColor;
    uniform float uScanY;
    uniform float uBoost;
  `;

  /**
   * Surface hologramme appliquée au MODÈLE HUMAIN RÉEL (.glb).
   * Contour de Fresnel lumineux + lignes de scan fines + bande de balayage,
   * corps translucide qui laisse voir l'intérieur → aspect hologramme médical.
   */
  private makeSurfaceMaterial(): THREE.ShaderMaterial {
    const m = new THREE.ShaderMaterial({
      transparent: true,
      depthWrite: false,
      side: THREE.DoubleSide,
      blending: THREE.NormalBlending,
      uniforms: {
        uTime: { value: 0 }, uColor: { value: new THREE.Color(0x0a5a92) },
        uScanY: { value: 1.9 }, uBoost: { value: 0 }
      },
      vertexShader: /* glsl */`
        varying vec3 vNormal; varying vec3 vView; varying float vY;
        void main() {
          vec4 wp = modelMatrix * vec4(position, 1.0);
          vNormal = normalize(mat3(modelMatrix) * normal);
          vView = normalize(cameraPosition - wp.xyz);
          vY = wp.y;
          gl_Position = projectionMatrix * viewMatrix * wp;
        }
      `,
      fragmentShader: /* glsl */`
        precision highp float;
        ${MedicalHologramComponent.COMMON_UNIFORMS}
        varying vec3 vNormal; varying vec3 vView; varying float vY;
        void main() {
          // rim-light de Fresnel : silhouette marquée (alpha)
          float fres = pow(1.0 - clamp(dot(normalize(vNormal), normalize(vView)), 0.0, 1.0), 2.0);
          // fines lignes de scan horizontales qui défilent
          float lines = smoothstep(0.5, 1.0, sin(vY * 220.0 - uTime * 4.0)) * 0.22;
          // bande de balayage médicale (teintée turquoise)
          float band = smoothstep(0.07, 0.0, abs(vY - uScanY)) * (0.6 + uBoost);
          // sur fond BLANC : la couleur reste sombre, l'alpha dessine contour + lignes + bande
          float a = clamp(fres * 0.85 + lines + band * 0.7, 0.0, 0.96);
          vec3 col = mix(uColor, vec3(0.0, 0.55, 0.62), band * 0.55);
          gl_FragColor = vec4(col, a);
        }
      `
    });
    this.shaderMats.push(m);
    return m;
  }

  /** Intérieur du corps : sombre, translucide, légèrement teinté (donne le volume). */
  private makeFillMaterial(): THREE.ShaderMaterial {
    const m = new THREE.ShaderMaterial({
      transparent: true,
      depthWrite: false,
      side: THREE.FrontSide,
      blending: THREE.NormalBlending,
      uniforms: {
        uTime: { value: 0 }, uColor: { value: new THREE.Color(0x0a5a92) },
        uScanY: { value: 1.9 }, uBoost: { value: 0 }
      },
      vertexShader: /* glsl */`
        varying vec3 vNormal; varying vec3 vView; varying float vY;
        void main() {
          vec4 wp = modelMatrix * vec4(position, 1.0);
          vNormal = normalize(mat3(modelMatrix) * normal);
          vView = normalize(cameraPosition - wp.xyz);
          vY = wp.y;
          gl_Position = projectionMatrix * viewMatrix * wp;
        }
      `,
      fragmentShader: /* glsl */`
        precision highp float;
        ${MedicalHologramComponent.COMMON_UNIFORMS}
        varying vec3 vNormal; varying vec3 vView; varying float vY;
        void main() {
          float fres = pow(1.0 - clamp(dot(normalize(vNormal), normalize(vView)), 0.0, 1.0), 3.0);
          float band = smoothstep(0.09, 0.0, abs(vY - uScanY)) * (0.5 + uBoost);
          // sur fond BLANC : corps en verre clair bleuté, contour (Fresnel) plus dense
          vec3 col = mix(vec3(0.82, 0.90, 0.97), uColor, fres * 0.55 + band * 0.4);
          float a = 0.08 + fres * 0.16 + band * 0.22;
          gl_FragColor = vec4(col, a);
        }
      `
    });
    this.shaderMats.push(m);
    return m;
  }

  /** Arêtes filaires lumineuses (rendues en wireframe). */
  private makeEdgeMaterial(): THREE.ShaderMaterial {
    const m = new THREE.ShaderMaterial({
      transparent: true,
      depthWrite: false,
      blending: THREE.NormalBlending,
      wireframe: true,
      uniforms: {
        uTime: { value: 0 }, uColor: { value: new THREE.Color(0x0a5a92) },
        uScanY: { value: 1.9 }, uBoost: { value: 0 }
      },
      vertexShader: /* glsl */`
        varying float vY;
        void main() {
          vec4 wp = modelMatrix * vec4(position, 1.0);
          vY = wp.y;
          gl_Position = projectionMatrix * viewMatrix * wp;
        }
      `,
      fragmentShader: /* glsl */`
        precision highp float;
        ${MedicalHologramComponent.COMMON_UNIFORMS}
        varying float vY;
        void main() {
          float band = smoothstep(0.10, 0.0, abs(vY - uScanY)) * (0.7 + uBoost);
          float flick = 0.85 + 0.15 * sin(uTime * 7.0 + vY * 5.0);
          // sur fond BLANC : lignes sombres, légèrement turquoise sur la bande de scan
          vec3 col = mix(uColor, vec3(0.0, 0.55, 0.62), band * 0.5);
          float a = (0.42 + band * 0.5) * flick;
          gl_FragColor = vec4(col, clamp(a, 0.0, 0.95));
        }
      `
    });
    this.shaderMats.push(m);
    return m;
  }

  /** Nœuds brillants à chaque sommet du maillage (THREE.Points). */
  private makeNodeMaterial(): THREE.ShaderMaterial {
    const m = new THREE.ShaderMaterial({
      transparent: true,
      depthWrite: false,
      blending: THREE.NormalBlending,
      uniforms: {
        uTime: { value: 0 }, uColor: { value: new THREE.Color(0x0e6aa6) },
        uScanY: { value: 1.9 }, uBoost: { value: 0 }, uSize: { value: 5.0 }
      },
      vertexShader: /* glsl */`
        ${MedicalHologramComponent.COMMON_UNIFORMS}
        uniform float uSize;
        attribute float aSeed;
        varying float vGlow; varying float vBand;
        void main() {
          vec4 wp = modelMatrix * vec4(position, 1.0);
          vBand = smoothstep(0.12, 0.0, abs(wp.y - uScanY));
          float tw = 0.6 + 0.4 * sin(uTime * 3.0 + aSeed * 12.56);
          vGlow = tw;
          vec4 mv = viewMatrix * wp;
          gl_PointSize = uSize * (0.7 + aSeed * 0.6) * (1.0 + vBand * 1.6 + uBoost) / -mv.z;
          gl_Position = projectionMatrix * mv;
        }
      `,
      fragmentShader: /* glsl */`
        precision highp float;
        ${MedicalHologramComponent.COMMON_UNIFORMS}
        varying float vGlow; varying float vBand;
        void main() {
          vec2 c = gl_PointCoord - 0.5;
          float d = length(c);
          if (d > 0.5) discard;
          float core = smoothstep(0.5, 0.0, d);
          vec3 col = uColor + vBand * vec3(0.4, 0.5, 0.3);
          gl_FragColor = vec4(col, core * (0.45 + vGlow * 0.55 + vBand * 0.6));
        }
      `
    });
    this.shaderMats.push(m);
    return m;
  }

  // =====================================================================
  //  CHARGEMENT DU MODÈLE HUMAIN RÉALISTE (.glb)
  // =====================================================================
  private initLoader(): void {
    this.loader = new GLTFLoader();
    // support des modèles compressés Draco (optionnel — décodeur CDN officiel)
    const draco = new DRACOLoader();
    draco.setDecoderPath('https://www.gstatic.com/draco/versioned/decoders/1.5.6/');
    this.loader.setDRACOLoader(draco);
  }

  /** Charge (ou réutilise) le modèle correspondant au genre, l'holo-shade et l'affiche. */
  private loadModel(gender: string): void {
    const key = gender || 'MALE';
    const path = MedicalHologramComponent.MODEL_PATHS[key];
    if (!path) return;

    // déjà en cache → bascule immédiate
    if (this.modelCache[key]) {
      this.showModel(this.modelCache[key]);
      return;
    }

    this.loader.load(
      path,
      (gltf) => {
        const model = this.prepareModel(gltf.scene);
        this.modelCache[key] = model;
        this.showModel(model);
      },
      undefined,
      () => {
        // pas de fichier .glb → on reste sur le corps filaire de secours
        this.usingFallback = true;
        this.fallback.visible = true;
        this.modelGroup.visible = false;
      }
    );
  }

  /** Normalise le modèle (taille ~1.8u, pieds à y=0, centré) et applique l'hologramme. */
  private prepareModel(src: THREE.Object3D): THREE.Group {
    const wrap = new THREE.Group();
    wrap.add(src);

    // recadrage : hauteur cible ≈ 1.8 unité monde, pieds posés à y=0, centré en X/Z
    const box = new THREE.Box3().setFromObject(src);
    const size = new THREE.Vector3(); box.getSize(size);
    const center = new THREE.Vector3(); box.getCenter(center);
    const targetH = 1.8;
    const s = size.y > 0.0001 ? targetH / size.y : 1;
    src.scale.setScalar(s);
    src.position.x -= center.x * s;
    src.position.z -= center.z * s;
    src.position.y -= box.min.y * s;

    // applique le matériau hologramme à tous les meshes du modèle réel
    src.traverse((o) => {
      const mesh = o as THREE.Mesh;
      if ((mesh as any).isMesh) {
        mesh.material = this.surfMat;
        mesh.frustumCulled = false;
      }
    });

    // proportions pour le circuit : largeur/profondeur du TRONC dérivées de la
    // hauteur (≈ proportions humaines) — la bbox X/Z inclurait bras et pose et
    // ferait déborder les vaisseaux ; on garde donc une demi-largeur de tronc fiable.
    (wrap as any).bloodDims = {
      H: targetH,
      W: targetH * 0.17,   // demi-largeur de tronc (~épaules) ≈ 0.17·H
      D: targetH * 0.11,   // demi-profondeur de tronc ≈ 0.11·H
      cx: 0, cz: 0         // recentré en X/Z
    };
    return wrap;
  }

  /** Affiche le modèle réel, masque le secours, et joue une transition d'apparition. */
  private showModel(model: THREE.Group): void {
    this.modelGroup.clear();
    this.modelGroup.add(model);
    this.usingFallback = false;
    this.modelGroup.visible = true;
    this.fallback.visible = false;

    // adapte le réseau vasculaire aux proportions de CE modèle
    this.bloodDims = (model as any).bloodDims || MedicalHologramComponent.FALLBACK_DIMS;
    this.rebuildCirculationGeometry();

    // apparition holographique (montée + léger fondu d'échelle)
    model.scale.setScalar(0.92);
    gsap.fromTo(model.scale, { x: 0.92, y: 0.92, z: 0.92 },
      { x: 1, y: 1, z: 1, duration: 0.9, ease: 'power3.out' });
  }

  // =====================================================================
  //  CORPS HUMAIN PROCÉDURAL (secours)
  // =====================================================================
  private lathe(profile: [number, number][], y0: number, seg = 11): THREE.LatheGeometry {
    const pts = profile.map(([r, y]) => new THREE.Vector2(Math.max(r, 0.0005), y));
    const g = new THREE.LatheGeometry(pts, seg);
    g.translate(0, y0, 0);
    g.computeVertexNormals();
    return g;
  }

  /**
   * Ajoute une partie du corps en réseau filaire :
   *  - une coque interne sombre translucide (volume),
   *  - un wireframe lumineux (arêtes),
   *  - des nœuds brillants sur chaque sommet (THREE.Points).
   */
  private addPart(geo: THREE.BufferGeometry, parent: THREE.Group): THREE.Mesh {
    const fill = new THREE.Mesh(geo, this.fillMat);
    const edge = new THREE.Mesh(geo, this.edgeMat);
    fill.add(edge);

    // nœuds : un point par sommet de la géométrie, avec une graine de scintillement
    const count = geo.getAttribute('position').count;
    const seeds = new Float32Array(count);
    for (let i = 0; i < count; i++) seeds[i] = (i * 0.6180339887) % 1; // dispersion déterministe
    const nodeGeo = new THREE.BufferGeometry();
    nodeGeo.setAttribute('position', geo.getAttribute('position'));
    nodeGeo.setAttribute('aSeed', new THREE.BufferAttribute(seeds, 1));
    const nodes = new THREE.Points(nodeGeo, this.nodeMat);
    fill.add(nodes);

    parent.add(fill);
    return fill;
  }

  private buildMaterials(): void {
    this.fillMat = this.makeFillMaterial();
    this.edgeMat = this.makeEdgeMaterial();
    this.nodeMat = this.makeNodeMaterial();
    this.surfMat = this.makeSurfaceMaterial();
  }

  /** Corps procédural de secours (affiché tant qu'aucun .glb réel n'est chargé). */
  private buildFallbackHuman(): void {
    this.upper = new THREE.Group();
    this.lower = new THREE.Group();
    this.fallback.add(this.upper, this.lower);

    // --- Tête --- (low-poly)
    const head = new THREE.IcosahedronGeometry(0.118, 2);
    head.scale(0.9, 1.14, 0.95);
    head.translate(0, 1.66, 0.01);
    this.addPart(head, this.upper);

    // --- Cou ---
    this.addPart(this.lathe([[0.052, 0], [0.06, 0.06], [0.07, 0.12]], 1.46, 9), this.upper);

    // --- Torse / poitrine (épaules large -> taille fine) ---
    const chest = this.lathe([
      [0.05, 0.00],   // haut épaules (se referme vers le cou)
      [0.19, 0.03],   // ligne d'épaule
      [0.205, 0.10],
      [0.185, 0.20],  // poitrine
      [0.155, 0.32],
      [0.135, 0.42],  // taille
      [0.12, 0.46]
    ], 1.00, 13);
    this.addPart(chest, this.upper);

    // --- Bassin / hanches ---
    const pelvis = this.lathe([
      [0.118, 0.00],
      [0.15, 0.07],
      [0.165, 0.15],  // hanches
      [0.15, 0.24],
      [0.10, 0.30]
    ], 0.72, 13);
    this.addPart(pelvis, this.lower);

    // --- Épaules (sphères de jonction) ---
    for (const sx of [-1, 1]) {
      const sh = new THREE.IcosahedronGeometry(0.07, 1);
      sh.translate(sx * 0.205, 1.44, 0);
      this.addPart(sh, this.upper);
    }

    // --- Bras (capsules épaule -> poignet) ---
    for (const sx of [-1, 1]) {
      const arm = new THREE.CapsuleGeometry(0.05, 0.56, 3, 10);
      arm.translate(0, -0.30, 0);
      const m = this.addPart(arm, this.upper);
      m.position.set(sx * 0.235, 1.44, 0);
      m.rotation.z = sx * 0.14;
    }

    // --- Mains ---
    for (const sx of [-1, 1]) {
      const hand = new THREE.IcosahedronGeometry(0.055, 1);
      hand.scale(1, 1.25, 0.7);
      const m = this.addPart(hand, this.upper);
      m.position.set(sx * 0.355, 0.86, 0);
    }

    // --- Jambes (cuisse+mollet en capsule) ---
    for (const sx of [-1, 1]) {
      const leg = new THREE.CapsuleGeometry(0.072, 0.62, 3, 12);
      leg.translate(0, -0.38, 0);
      const m = this.addPart(leg, this.lower);
      m.position.set(sx * 0.092, 0.78, 0);
    }
    // --- Pieds ---
    for (const sx of [-1, 1]) {
      const foot = new THREE.IcosahedronGeometry(0.062, 1);
      foot.scale(1, 0.6, 1.7);
      const m = this.addPart(foot, this.lower);
      m.position.set(sx * 0.092, 0.04, 0.05);
    }

    // --- Cœur (apparaît avec la circulation) ---
    this.heart = new THREE.Mesh(
      new THREE.SphereGeometry(0.055, 24, 20),
      new THREE.MeshBasicMaterial({
        color: 0xd11f3f, transparent: true, opacity: 0,
        blending: THREE.NormalBlending, depthWrite: false
      })
    );
    this.heart.scale.set(1.1, 1.25, 0.9);
    this.heart.position.set(-0.03, 1.24, 0.07);
    this.root.add(this.heart);
  }

  // =====================================================================
  //  CIRCULATION SANGUINE — réseau vasculaire (vaisseaux + globules circulants)
  // =====================================================================

  /**
   * Construit le réseau vasculaire À PARTIR DES PROPORTIONS du corps affiché
   * (this.bloodDims). Tous les repères sont des fractions de la hauteur H et de
   * la demi-largeur W → le circuit s'adapte au modèle réel (male/female/fallback),
   * quelles que soient sa carrure et sa taille.
   */
  private buildVesselCurves(): THREE.CatmullRomCurve3[] {
    const { H, W, D, cx, cz } = this.bloodDims;
    // repères anatomiques en fraction de hauteur (proportions humaines standard)
    const yHeart = 0.69 * H, yHead = 0.93 * H, yNeck = 0.84 * H;
    const yChest = 0.78 * H, yWaist = 0.58 * H, yHip = 0.50 * H;
    const front = D * 0.30, back = -D * 0.30;       // amplitude avant/arrière contenue
    // X(x): décalage latéral RÉDUIT (max ~0.32·W) → ne sort jamais de la silhouette
    const X = (f: number) => cx + f * W;
    const V = (x: number, y: number, z: number) => new THREE.Vector3(x, y, cz + z);

    const heart = new THREE.Vector3(X(-0.06), yHeart, cz + front * 0.5);
    const curves: THREE.CatmullRomCurve3[] = [];

    // 1) AORTE / colonne centrale — cœur → tête → cœur (boucle fermée, sur l'axe)
    curves.push(new THREE.CatmullRomCurve3([
      heart,
      V(X(0), yChest, front * 0.7),
      V(X(0.04), yNeck, front * 0.4),
      V(X(0), yHead, front * 0.1),
      V(X(-0.04), yNeck, back * 0.4),
      V(X(0), yChest, back * 0.6),
      V(X(-0.06), yHeart, back * 0.4),
      heart
    ], true));

    // 2) BOUCLE THORACIQUE gauche/droite — reste près du sternum (≤ 0.3·W)
    for (const sx of [-1, 1]) {
      curves.push(new THREE.CatmullRomCurve3([
        heart,
        V(X(sx * 0.18), yChest * 0.99, front * 0.6),
        V(X(sx * 0.3), (yChest + yNeck) / 2, front * 0.3),
        V(X(sx * 0.26), yNeck * 0.99, 0),
        V(X(sx * 0.22), (yChest + yNeck) / 2, back * 0.3),
        V(X(sx * 0.12), yChest * 0.98, back * 0.4),
        heart
      ], true));
    }

    // 3) BOUCLE ABDOMINALE gauche/droite — descend vers le bassin, reste centrée
    for (const sx of [-1, 1]) {
      curves.push(new THREE.CatmullRomCurve3([
        heart,
        V(X(sx * 0.1), (yHeart + yWaist) / 2, front * 0.6),
        V(X(sx * 0.22), yWaist, front * 0.3),
        V(X(sx * 0.2), yHip, front * 0.1),
        V(X(sx * 0.2), yHip, back * 0.2),
        V(X(sx * 0.1), yWaist, back * 0.4),
        V(X(sx * 0.05), (yHeart + yWaist) / 2, back * 0.5),
        heart
      ], true));
    }

    return curves;
  }

  /** Recâble vaisseaux + globules quand le corps (proportions) change. */
  private rebuildCirculationGeometry(): void {
    this.bloodCurves = this.buildVesselCurves();

    // lignes de vaisseaux : on remplace la géométrie de chaque Line existante
    for (let c = 0; c < this.vesselLines.length; c++) {
      const pts = this.bloodCurves[c].getPoints(MedicalHologramComponent.VESSEL_SEGMENTS);
      this.vesselLines[c].geometry.dispose();
      this.vesselLines[c].geometry = new THREE.BufferGeometry().setFromPoints(pts);
    }

    // globules : on les replace immédiatement sur leur paramètre courant
    if (this.blood) {
      const arr = (this.blood.geometry.getAttribute('position') as THREE.BufferAttribute).array as Float32Array;
      for (let i = 0; i < this.bloodVel.length; i++) {
        const p = this.bloodCurves[this.bloodCurveIdx[i]].getPointAt(this.bloodT[i]);
        arr[i * 3] = p.x; arr[i * 3 + 1] = p.y; arr[i * 3 + 2] = p.z;
      }
      (this.blood.geometry.getAttribute('position') as THREE.BufferAttribute).needsUpdate = true;
    }

    // le cœur suit aussi le nouveau repère cardiaque
    if (this.heart) {
      const h = this.bloodCurves[0].getPointAt(0);
      this.heart.position.copy(h);
    }
  }

  private buildCirculation(): void {
    this.bloodCurves = this.buildVesselCurves();

    // 1) LIGNES DE VAISSEAUX (tracé lumineux visible, donne la lisibilité du circuit)
    this.vessels = new THREE.Group();
    for (const curve of this.bloodCurves) {
      const pts = curve.getPoints(MedicalHologramComponent.VESSEL_SEGMENTS);
      const g = new THREE.BufferGeometry().setFromPoints(pts);
      const mat = new THREE.LineBasicMaterial({
        color: 0xc41f3f, transparent: true, opacity: 0,
        blending: THREE.NormalBlending, depthWrite: false
      });
      this.vesselMats.push(mat);
      const line = new THREE.Line(g, mat);
      this.vesselLines.push(line);
      this.vessels.add(line);
    }
    this.root.add(this.vessels);

    // 2) GLOBULES qui circulent le long des vaisseaux
    const PER = 150;                       // globules par vaisseau
    const N = this.bloodCurves.length * PER;
    const pos = new Float32Array(N * 3);
    const seed = new Float32Array(N);
    this.bloodVel = new Float32Array(N);
    this.bloodT = new Float32Array(N);
    this.bloodCurveIdx = new Int16Array(N);

    let i = 0;
    for (let c = 0; c < this.bloodCurves.length; c++) {
      const curve = this.bloodCurves[c];
      for (let k = 0; k < PER; k++, i++) {
        const t = k / PER;
        this.bloodCurveIdx[i] = c;
        this.bloodT[i] = t;
        this.bloodVel[i] = 0.10 + (k % 7) / 7 * 0.10; // flux régulier, légère dispersion
        seed[i] = (i * 0.6180339887) % 1;
        const p = curve.getPointAt(t);
        pos[i * 3] = p.x; pos[i * 3 + 1] = p.y; pos[i * 3 + 2] = p.z;
      }
    }

    const geo = new THREE.BufferGeometry();
    geo.setAttribute('position', new THREE.BufferAttribute(pos, 3));
    geo.setAttribute('aSeed', new THREE.BufferAttribute(seed, 1));

    this.bloodMat = new THREE.ShaderMaterial({
      transparent: true,
      depthWrite: false,
      blending: THREE.NormalBlending,
      uniforms: {
        uTime: { value: 0 },
        uOpacity: { value: 0 },
        uSize: { value: (this.host.nativeElement.clientHeight || 480) * 0.05 }
      },
      vertexShader: /* glsl */`
        attribute float aSeed;
        uniform float uTime;
        uniform float uSize;
        varying float vGlow;
        void main() {
          // pulsation synchronisée au battement du cœur (~ même fréquence)
          vGlow = 0.55 + 0.45 * sin(uTime * 6.0 + aSeed * 12.56);
          vec4 mv = modelViewMatrix * vec4(position, 1.0);
          gl_PointSize = uSize * (0.55 + aSeed * 0.7) / -mv.z;
          gl_Position = projectionMatrix * mv;
        }
      `,
      fragmentShader: /* glsl */`
        precision highp float;
        uniform float uOpacity;
        varying float vGlow;
        void main() {
          vec2 c = gl_PointCoord - 0.5;
          float d = length(c);
          if (d > 0.5) discard;
          // cœur dense + halo : globule net et lisible sur fond BLANC (rouges sombres)
          float core = smoothstep(0.5, 0.0, d);
          float halo = smoothstep(0.5, 0.15, d);
          vec3 col = mix(vec3(0.52, 0.0, 0.06), vec3(0.86, 0.14, 0.24), vGlow);
          float a = (core * 0.92 + halo * 0.22) * uOpacity * (0.6 + vGlow * 0.4);
          gl_FragColor = vec4(col, a);
        }
      `
    });

    this.blood = new THREE.Points(geo, this.bloodMat);
    this.root.add(this.blood);
  }

  // =====================================================================
  //  ÉTAT -> ANIMATIONS
  // =====================================================================
  private applyGender(immediate = false): void {
    const g = this.gender;

    // 1) charge le MODÈLE HUMAIN RÉEL correspondant (si genre défini)
    if (g && g !== this.loadedGender) {
      this.loadedGender = g;
      this.loadModel(g);
    }

    // 2) couleur hologramme — teintes SOMBRES (lisibles sur fond blanc)
    const color = g === 'FEMALE' ? new THREE.Color(0xb0357a)   // rose profond
                : g === 'MALE'   ? new THREE.Color(0x0a5a92)   // bleu médité. foncé
                :                  new THREE.Color(0x117d8f);  // teal foncé
    const node = color.clone().lerp(new THREE.Color(0x05263e), 0.30);
    const d = immediate ? 0 : 1.0;
    gsap.to(this.surfMat.uniforms['uColor'].value, { r: color.r, g: color.g, b: color.b, duration: d || 0.4 });
    gsap.to(this.edgeMat.uniforms['uColor'].value, { r: color.r, g: color.g, b: color.b, duration: d || 0.4 });
    gsap.to(this.nodeMat.uniforms['uColor'].value, { r: node.r, g: node.g, b: node.b, duration: d || 0.4 });
    gsap.to(this.fillMat.uniforms['uColor'].value, { r: color.r, g: color.g, b: color.b, duration: d || 0.4 });

    // 3) morphing du SECOURS uniquement (carrure/bassin) ; le modèle réel
    //    porte déjà l'anatomie correcte selon le genre.
    const upX = g === 'MALE' ? 1.12 : g === 'FEMALE' ? 0.94 : 1.0;
    const loX = g === 'FEMALE' ? 1.16 : g === 'MALE' ? 0.95 : 1.04;
    gsap.to(this.upper.scale, { x: upX, z: upX * 0.96, duration: d, ease: 'elastic.out(1,0.7)' });
    gsap.to(this.lower.scale, { x: loX, z: loX * 0.94, duration: d, ease: 'elastic.out(1,0.7)' });
  }

  private applyMorph(immediate = false): void {
    const h = this.heightCm && this.heightCm > 0 ? this.heightCm : 175;
    const w = this.weightKg && this.weightKg > 0 ? this.weightKg : 70;
    // taille -> élongation verticale
    const sy = THREE.MathUtils.clamp(0.86 + (h - 140) / 70 * 0.26, 0.84, 1.18);
    // corpulence -> via IMC : largeur/profondeur du tronc et des membres
    const bmi = w / Math.pow(h / 100, 2);
    const girth = THREE.MathUtils.clamp(0.78 + (bmi - 18.5) / 16 * 0.5, 0.78, 1.32);

    const d = immediate ? 0 : 1.1;
    gsap.to(this.root.scale, { y: sy, duration: d, ease: 'power3.out' });
    gsap.to(this.anim, { girth, duration: d, ease: 'power2.out' });
  }

  private applyBlood(immediate = false): void {
    const on = this.bloodGroup ? 1 : 0;
    const d = immediate ? 0 : 0.9;
    gsap.to(this.anim, { bloodOn: on, duration: d, ease: 'power2.out' });
    gsap.to(this.heart.material as THREE.Material, { opacity: on ? 0.9 : 0, duration: d } as any);
    // tracé des vaisseaux : plus marqué pour rester lisible sur fond blanc
    for (const m of this.vesselMats) {
      gsap.to(m, { opacity: on ? 0.55 : 0, duration: d } as any);
    }
  }

  // =====================================================================
  //  BOUCLE DE RENDU
  // =====================================================================
  private onPointer = (e: PointerEvent) => {
    const r = this.host.nativeElement.getBoundingClientRect();
    this.pointer.tx = ((e.clientX - r.left) / r.width - 0.5) * 0.5;
    this.pointer.ty = ((e.clientY - r.top) / r.height - 0.5) * 0.3;
  };

  private animate = (): void => {
    this.raf = requestAnimationFrame(this.animate);
    const t = this.clock.getElapsedTime();
    const dt = Math.min(this.clock.getDelta(), 0.05);

    // rotation : auto-spin + parallaxe pointeur
    this.pointer.x += (this.pointer.tx - this.pointer.x) * 0.05;
    this.pointer.y += (this.pointer.ty - this.pointer.y) * 0.05;
    if (this.root) {
      this.root.rotation.y = (this.reduced ? 0 : t * 0.28) + this.pointer.x;
      this.root.rotation.x = this.pointer.y * 0.4;
      // corpulence (IMC) appliquée globalement en X/Z ; la différenciation
      // de genre (carrure/bassin) reste portée par les sous-groupes upper/lower,
      // et la taille par root.scale.y (tweenée dans applyMorph).
      this.root.scale.x = this.anim.girth;
      this.root.scale.z = this.anim.girth;
    }

    // balayage de scan vertical
    const span = this.bloodBounds.y1 + 0.4;
    this.anim.scanY = 0.1 + (0.5 + 0.5 * Math.sin(t * (0.8 + this.anim.scanBoost * 1.6))) * span;

    // uniforms shaders (fill + edge + node)
    for (const m of this.shaderMats) {
      m.uniforms['uTime'].value = t;
      m.uniforms['uScanY'].value = this.anim.scanY;
      m.uniforms['uBoost'].value = this.anim.scanBoost;
    }

    // cœur qui bat
    if (this.heart) {
      const beat = 1 + Math.sin(t * 6.0) * 0.08 + Math.max(0, Math.sin(t * 6.0 - 0.4)) * 0.06;
      this.heart.scale.set(1.1 * beat, 1.25 * beat, 0.9 * beat);
    }

    // globules circulant le long des vaisseaux
    if (this.blood) {
      this.bloodMat.uniforms['uTime'].value = t;
      this.bloodMat.uniforms['uOpacity'].value = this.anim.bloodOn;
      if (this.anim.bloodOn > 0.01) {
        const arr = (this.blood.geometry.getAttribute('position') as THREE.BufferAttribute).array as Float32Array;
        // poussée systolique : le flux accélère à chaque battement de cœur
        const pulse = 0.7 + 0.6 * Math.max(0, Math.sin(t * 6.0));
        for (let i = 0; i < this.bloodVel.length; i++) {
          let tParam = this.bloodT[i] + this.bloodVel[i] * pulse * dt;
          if (tParam >= 1) tParam -= 1;            // boucle fermée → retour au cœur
          this.bloodT[i] = tParam;
          const p = this.bloodCurves[this.bloodCurveIdx[i]].getPointAt(tParam);
          arr[i * 3] = p.x; arr[i * 3 + 1] = p.y; arr[i * 3 + 2] = p.z;
        }
        (this.blood.geometry.getAttribute('position') as THREE.BufferAttribute).needsUpdate = true;
      }
    }

    this.renderer.render(this.scene, this.camera);
  };

  private resize(): void {
    const el = this.host.nativeElement;
    const w = el.clientWidth, h = el.clientHeight;
    if (!w || !h) return;
    this.renderer.setSize(w, h);
    this.camera.aspect = w / h;
    this.camera.updateProjectionMatrix();
    if (this.bloodMat) this.bloodMat.uniforms['uSize'].value = h * 0.04;
  }
}
