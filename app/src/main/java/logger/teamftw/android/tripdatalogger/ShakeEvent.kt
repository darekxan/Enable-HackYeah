package logger.teamftw.android.tripdatalogger

data class ShakeEvent(var level: Level, var seconds: Long) {
    enum class Level( val resId: Int) {
        HIGH(android.R.color.holo_red_light), MEDIUM(android.R.color.holo_orange_light), LOW(android.R.color.holo_green_dark)
    }
}
