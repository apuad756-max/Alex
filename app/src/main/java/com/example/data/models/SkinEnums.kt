package com.example.data.models

enum class BodyShape(val displayName: String) {
    ORB("Aero Orb"),
    DIAMOND("Prism Diamond"),
    SHURIKEN("Cyber Shuriken"),
    HEXAGON("Mecha Hex"),
    SHIELD("Aegis Shield"),
    STAR("Nova Star"),
    CUSTOM_GALLERY("Gallery Sprite")
}

enum class TrailType(val displayName: String) {
    CYBER_SPARKS("Cyber Sparks"),
    PLASMA_FLAME("Plasma Flame"),
    RAINBOW_DUST("Rainbow Dust"),
    VOID_SMOKE("Void Smoke"),
    NEON_STREAM("Neon Stream")
}

enum class AccessoryType(val displayName: String) {
    NONE("None"),
    CYBER_VISOR("Cyber Visor"),
    NEON_CROWN("Neon Crown"),
    ORBITING_ORBS("Orbiting Spheres"),
    ANGEL_WINGS("Energy Wings"),
    CYBER_HORNS("Cyber Horns"),
    NINJA_BANDANA("Ninja Bandana")
}

enum class WeaponFx(val displayName: String) {
    BLADE_SLASH("Neon Blade"),
    DUAL_SABERS("Dual Sabers"),
    STAR_SHURIKEN("Star Shuriken"),
    PLASMA_PULSE("Plasma Nova")
}

enum class GameMode(val displayName: String, val description: String) {
    ENDLESS("Endless Rush", "Survive escalating waves of rogue drones and laser hazards"),
    BLITZ_60("60s Blitz", "Fast-paced score attack with 2x multipliers and frenzy spawns"),
    BOSS_TRIAL("Boss Trial", "Face off against colossal cyber titans in high-stakes duels")
}
