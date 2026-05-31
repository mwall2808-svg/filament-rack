package com.example.thefilamentrack

sealed class Screen(val route: String) {
    object SpoolList : Screen("spool_list")
    object AddSpool : Screen("add_spool")
    object EditSpool : Screen("edit_spool/{spoolId}") {
        fun createRoute(spoolId: Int) = "edit_spool/$spoolId"
    }
    object SpoolDetail : Screen("spool_detail/{spoolId}") {
        fun createRoute(spoolId: Int) = "spool_detail/$spoolId"
    }
    object LogUsage : Screen("log_usage/{spoolId}") {
        fun createRoute(spoolId: Int) = "log_usage/$spoolId"
    }
}