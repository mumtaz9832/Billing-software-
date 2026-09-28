package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.dialogs.InvoicePrintDialog
import com.example.ui.screens.AiAssistantScreen
import com.example.ui.screens.BillingScreen
import com.example.ui.screens.CustomerKhataScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.InventoryScreen
import com.example.ui.screens.PosScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.VyaparTealPrimary
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.VyaparViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                VyaparApp()
            }
        }
    }
}

@Composable
fun VyaparApp(viewModel: VyaparViewModel = viewModel()) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val profile by viewModel.profile.collectAsState()
    val isUnlocked by viewModel.isUnlocked.collectAsState()
    val selectedInvoice by viewModel.selectedInvoice.collectAsState()

    // Handle system back navigation
    BackHandler(enabled = currentScreen != AppScreen.DASHBOARD) {
        viewModel.navigateBack()
    }

    // App Security PIN Lock Check
    val requiresPin = profile?.isPinLockEnabled == true && !isUnlocked
    if (requiresPin) {
        PinLockDialog(
            correctPin = profile?.pinCode ?: "1234",
            onSuccess = {
                viewModel.unlockApp(it, profile?.pinCode ?: "1234")
            }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (currentScreen != AppScreen.POS) {
                VyaparBottomBar(
                    currentScreen = currentScreen,
                    onNavigate = { viewModel.navigateTo(it) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                AppScreen.DASHBOARD -> DashboardScreen(viewModel = viewModel)
                AppScreen.BILLING -> BillingScreen(viewModel = viewModel)
                AppScreen.POS -> PosScreen(viewModel = viewModel)
                AppScreen.INVENTORY -> InventoryScreen(viewModel = viewModel)
                AppScreen.KHATA -> CustomerKhataScreen(viewModel = viewModel)
                AppScreen.REPORTS -> ReportsScreen(viewModel = viewModel)
                AppScreen.AI_ASSISTANT -> AiAssistantScreen(viewModel = viewModel)
                AppScreen.SETTINGS -> SettingsScreen(viewModel = viewModel)
            }

            // Print / WhatsApp Dialog modal
            selectedInvoice?.let { inv ->
                profile?.let { prof ->
                    InvoicePrintDialog(
                        invoice = inv,
                        profile = prof,
                        onDismiss = { viewModel.selectedInvoice.value = null }
                    )
                }
            }
        }
    }
}

@Composable
fun VyaparBottomBar(
    currentScreen: AppScreen,
    onNavigate: (AppScreen) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        modifier = Modifier.testTag("main_bottom_nav_bar")
    ) {
        val navItems = listOf(
            NavItem(AppScreen.DASHBOARD, "Home", Icons.Default.Home),
            NavItem(AppScreen.BILLING, "Billing", Icons.Default.Receipt),
            NavItem(AppScreen.POS, "POS", Icons.Default.PointOfSale),
            NavItem(AppScreen.INVENTORY, "Stock", Icons.Default.Inventory2),
            NavItem(AppScreen.KHATA, "Khata", Icons.Default.People),
            NavItem(AppScreen.REPORTS, "Reports", Icons.Default.Analytics)
        )

        navItems.forEach { item ->
            val isSelected = currentScreen == item.screen
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(item.screen) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = VyaparTealPrimary,
                    selectedTextColor = VyaparTealPrimary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}

data class NavItem(
    val screen: AppScreen,
    val label: String,
    val icon: ImageVector
)

@Composable
fun PinLockDialog(
    correctPin: String,
    onSuccess: (String) -> Unit
) {
    var pinInput by remember { mutableStateOf("") }
    var hasError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = { /* Force unlock */ }) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = VyaparTealPrimary, modifier = Modifier.size(28.dp))
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(text = "Enter Security PIN", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(text = "App is locked for data protection", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = pinInput,
                    onValueChange = {
                        if (it.length <= 4) {
                            pinInput = it
                            hasError = false
                        }
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    isError = hasError,
                    label = { Text("4-Digit PIN") },
                    modifier = Modifier.width(160.dp)
                )

                if (hasError) {
                    Text(text = "Incorrect PIN. Try again.", color = Color.Red, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        if (pinInput == correctPin) {
                            onSuccess(pinInput)
                        } else {
                            hasError = true
                        }
                    },
                    enabled = pinInput.length == 4,
                    colors = ButtonDefaults.buttonColors(containerColor = VyaparTealPrimary),
                    modifier = Modifier.fillMaxWidth().height(46.dp)
                ) {
                    Text("Unlock App", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
