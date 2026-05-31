package com.example.thefilamentrack

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.thefilamentrack.nfc.NfcManager
import com.example.thefilamentrack.ui.AddEditSpoolScreen
import com.example.thefilamentrack.ui.LogUsageScreen
import com.example.thefilamentrack.ui.SpoolDetailScreen
import com.example.thefilamentrack.ui.SpoolListScreen
import com.example.thefilamentrack.ui.theme.ThefilamentrackTheme

class MainActivity : ComponentActivity() {

    private var nfcAdapter: NfcAdapter? = null
    private lateinit var viewModel: SpoolViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        viewModel = ViewModelProvider(this)[SpoolViewModel::class.java]
        enableEdgeToEdge()

        setContent {
            ThefilamentrackTheme {
                val navController = rememberNavController()

                // Route NFC tags scanned on the SpoolList screen to LogUsage.
                // Other screens subscribe to viewModel.nfcTag directly via their own LaunchedEffect.
                LaunchedEffect(Unit) {
                    viewModel.nfcTag.collect { tag ->
                        val route = navController.currentBackStackEntry?.destination?.route
                        if (route == Screen.SpoolList.route) {
                            val writtenId = NfcManager.readSpoolId(tag)
                            val spool = if (writtenId != null) {
                                viewModel.getSpoolById(writtenId.toIntOrNull() ?: -1)
                            } else {
                                viewModel.getSpoolByTag(NfcManager.getTagId(tag))
                            }
                            spool?.let {
                                navController.navigate(Screen.LogUsage.createRoute(it.id))
                            }
                        }
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = Screen.SpoolList.route
                ) {
                    composable(Screen.SpoolList.route) {
                        SpoolListScreen(
                            viewModel = viewModel,
                            onAddSpool = { navController.navigate(Screen.AddSpool.route) },
                            onSpoolClick = { id ->
                                navController.navigate(Screen.SpoolDetail.createRoute(id))
                            }
                        )
                    }

                    composable(Screen.AddSpool.route) {
                        AddEditSpoolScreen(
                            viewModel = viewModel,
                            spoolId = null,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(Screen.SpoolDetail.route) { backStack ->
                        val spoolId =
                            backStack.arguments?.getString("spoolId")?.toIntOrNull() ?: 0
                        SpoolDetailScreen(
                            viewModel = viewModel,
                            spoolId = spoolId,
                            onBack = { navController.popBackStack() },
                            onEdit = { id ->
                                navController.navigate(Screen.EditSpool.createRoute(id))
                            }
                        )
                    }

                    composable(Screen.EditSpool.route) { backStack ->
                        val spoolId =
                            backStack.arguments?.getString("spoolId")?.toIntOrNull()
                        AddEditSpoolScreen(
                            viewModel = viewModel,
                            spoolId = spoolId,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(Screen.LogUsage.route) { backStack ->
                        val spoolId =
                            backStack.arguments?.getString("spoolId")?.toIntOrNull() ?: 0
                        LogUsageScreen(
                            viewModel = viewModel,
                            spoolId = spoolId,
                            onDone = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val intent = Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pending = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE)
        val filters = arrayOf(
            IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED).apply {
                try { addDataType("*/*") } catch (_: Exception) {}
            },
            IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED),
            IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED)
        )
        nfcAdapter?.enableForegroundDispatch(this, pending, filters, null)
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(this)
    }

    @Suppress("DEPRECATION")
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.action in listOf(
                NfcAdapter.ACTION_NDEF_DISCOVERED,
                NfcAdapter.ACTION_TECH_DISCOVERED,
                NfcAdapter.ACTION_TAG_DISCOVERED
            )
        ) {
            val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
            if (tag != null) {
                viewModel.dispatchNfcTag(tag)
            }
        }
    }
}
