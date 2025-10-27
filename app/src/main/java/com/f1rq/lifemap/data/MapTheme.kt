package com.f1rq.lifemap.data

import org.osmdroid.tileprovider.tilesource.XYTileSource

enum class MapTheme(val displayName: String) {
    POSITRON("Light"),
    CARTO_DARK("Black"),
    OSM_STANDARD("Standard"),
}

fun MapTheme.toTileSource(): XYTileSource {
    return when (this) {
        MapTheme.POSITRON -> XYTileSource(
            "CartoDB_Positron",
            1, 19, 256, ".png",
            arrayOf("https://a.basemaps.cartocdn.com/light_all/")
        )

        MapTheme.CARTO_DARK -> XYTileSource(
            "CartoDB_DarkMatter",
            1, 19, 256, ".png",
            arrayOf("https://a.basemaps.cartocdn.com/dark_all/")
        )

        MapTheme.OSM_STANDARD -> XYTileSource(
            "OSM_Standard",
            1, 19, 256, ".png",
            arrayOf("https://tile.openstreetmap.org/")
        )
    }
}