# Modèles 3D de l'hologramme médical

Dépose ici DEUX fichiers de corps humain réaliste au format **.glb** :

    male.glb     → corps masculin (vue debout, T-pose ou A-pose)
    female.glb   → corps féminin

Le composant les charge automatiquement, les centre, et applique
le traitement hologramme (matériau filaire bleu, lignes de scan,
nœuds lumineux, circulation sanguine) PAR-DESSUS le vrai maillage.
C'est le modèle réaliste qui donne l'aspect "humain réel".

------------------------------------------------------------------
OÙ TROUVER DES MODÈLES GRATUITS (réalistes, libres de droits)
------------------------------------------------------------------

1) READY PLAYER ME  (le plus simple — avatar humain réaliste)
   https://readyplayer.me/
   - Crée un avatar corps entier (homme puis femme)
   - Récupère l'URL .glb (ex: https://models.readyplayer.me/<id>.glb)
   - Télécharge le .glb, renomme-le male.glb / female.glb

2) SKETCHFAB  (filtre licence "Downloadable" + "CC0/CC-BY")
   https://sketchfab.com/search?q=human+body&type=models&licenses=...
   - Cherche "human body base mesh", "anatomy man/woman"
   - Télécharge en glTF (.glb), renomme

3) MIXAMO  (Adobe, gratuit — personnages humains réalistes)
   https://www.mixamo.com/
   - Choisis un personnage (ex: "Y Bot" non, prends un humain texturé)
   - Télécharge en FBX puis convertis en .glb (https://github.khronos.org/glTF-Sample-Viewer/ ou Blender)

4) QUATERNIUS / KENNEY  (low-poly mais propres, CC0)

------------------------------------------------------------------
CONSEILS TECHNIQUES
------------------------------------------------------------------
- Format .glb (binaire, tout-en-un). Pas .gltf+bin séparés.
- Modèle DEBOUT, axe Y vers le haut, regardant +Z (vers la caméra).
- Poids idéal < 8 Mo par fichier (sinon chargement lent).
- Si le modèle est en Draco-compressed, ça marche aussi (DRACOLoader inclus).
- Pas de fichier ? Le composant affiche un corps filaire de secours
  (fallback procédural) en attendant que tu déposes les .glb.
