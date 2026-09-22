package id.mport.maps.ui.settings

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.mport.maps.BuildConfig
import id.mport.maps.R
import id.mport.maps.data.AppLanguage
import id.mport.maps.domain.unit.UnitMode
import id.mport.maps.map.MapTypeHelper
import id.mport.maps.viewmodel.SurveyViewModel

private enum class SettingsPage { MAIN, ABOUT, PRIVACY, LICENSE, SECURITY }

@Composable
fun SettingsScreen(
    padding: PaddingValues,
    vm: SurveyViewModel,
    onOpenHistory: (() -> Unit)? = null,
    onSubPageChanged: ((onSubPage: Boolean, goBack: (() -> Unit)?) -> Unit)? = null
) {
    val settings by vm.settings.collectAsStateWithLifecycle()
    var page by remember { mutableStateOf(SettingsPage.MAIN) }
    var showMapTypeDialog by remember { mutableStateOf(false) }
    var showUnitDialog by remember { mutableStateOf(false) }
    var showLangDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Report nested page to parent for system Back handling
    val goBackToMain: () -> Unit = remember {{ page = SettingsPage.MAIN }}
    androidx.compose.runtime.LaunchedEffect(page) {
        val onSub = page != SettingsPage.MAIN
        onSubPageChanged?.invoke(onSub, if (onSub) goBackToMain else null)
    }
    androidx.compose.runtime.DisposableEffect(Unit) {
        onDispose { onSubPageChanged?.invoke(false, null) }
    }

    // Hardware/gesture back while on About/Privacy/License/Security
    androidx.activity.compose.BackHandler(enabled = page != SettingsPage.MAIN) {
        page = SettingsPage.MAIN
    }

    when (page) {
        SettingsPage.ABOUT -> AboutPage(
            padding = padding,
            onBack = { page = SettingsPage.MAIN }
        )
        SettingsPage.PRIVACY -> PrivacyPage(
            padding = padding,
            onBack = { page = SettingsPage.MAIN }
        )
        SettingsPage.LICENSE -> LicensePage(
            padding = padding,
            onBack = { page = SettingsPage.MAIN }
        )
        SettingsPage.SECURITY -> SecurityPage(
            padding = padding,
            onBack = { page = SettingsPage.MAIN }
        )
        SettingsPage.MAIN -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = stringResource(R.string.settings_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )

                SettingsItem(
                    icon = Icons.Default.Bookmark,
                    title = stringResource(R.string.settings_saved_list),
                    onClick = { onOpenHistory?.invoke() }
                )
                HorizontalDivider()
                SettingsItem(
                    icon = Icons.Default.Map,
                    title = stringResource(R.string.settings_map_type),
                    subtitle = MapTypeHelper.label(settings.mapType),
                    onClick = { showMapTypeDialog = true }
                )
                HorizontalDivider()
                SettingsItem(
                    icon = Icons.Default.Straighten,
                    title = stringResource(R.string.settings_units),
                    subtitle = settings.unit.label,
                    onClick = { showUnitDialog = true }
                )
                HorizontalDivider()
                SettingsItem(
                    icon = Icons.Default.MyLocation,
                    title = stringResource(R.string.settings_gps),
                    onClick = {
                        runCatching {
                            context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                        }
                    }
                )
                HorizontalDivider()
                SettingsItem(
                    icon = Icons.Default.Language,
                    title = stringResource(R.string.settings_language),
                    subtitle = when (settings.language) {
                        AppLanguage.ENGLISH -> stringResource(R.string.lang_english)
                        AppLanguage.INDONESIAN -> stringResource(R.string.lang_indonesian)
                        AppLanguage.SYSTEM -> stringResource(R.string.lang_system)
                    },
                    onClick = { showLangDialog = true }
                )
                HorizontalDivider()
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = stringResource(R.string.settings_about),
                    onClick = { page = SettingsPage.ABOUT }
                )
                HorizontalDivider()
                SettingsItem(
                    icon = Icons.Default.Policy,
                    title = stringResource(R.string.settings_privacy),
                    onClick = { page = SettingsPage.PRIVACY }
                )
                HorizontalDivider()
                SettingsItem(
                    icon = Icons.Default.Description,
                    title = stringResource(R.string.settings_license),
                    onClick = { page = SettingsPage.LICENSE }
                )
                HorizontalDivider()
                SettingsItem(
                    icon = Icons.Default.Security,
                    title = stringResource(R.string.settings_security),
                    onClick = { page = SettingsPage.SECURITY }
                )
                HorizontalDivider()

                // Theme toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DarkMode, contentDescription = null, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(16.dp))
                        Text(stringResource(R.string.settings_dark_theme), style = MaterialTheme.typography.bodyLarge)
                    }
                    Switch(
                        checked = settings.darkTheme,
                        onCheckedChange = { vm.setDarkTheme(it) }
                    )
                }
                HorizontalDivider()

                Spacer(Modifier.height(24.dp))
                Text(
                    text = stringResource(R.string.version_line),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Text(
                    text = if (BuildConfig.HAS_MAPS_KEY)
                        stringResource(R.string.api_key_ok)
                    else
                        stringResource(R.string.api_key_missing),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
                Spacer(Modifier.height(32.dp))
            }
        }
    }

    if (showMapTypeDialog) {
        MapTypeDialog(
            current = settings.mapType,
            onSelect = {
                vm.setMapType(it)
                showMapTypeDialog = false
            },
            onDismiss = { showMapTypeDialog = false }
        )
    }

    if (showUnitDialog) {
        UnitSelectDialog(
            current = settings.unit,
            onSelect = {
                vm.setUnit(it)
                showUnitDialog = false
            },
            onDismiss = { showUnitDialog = false }
        )
    }

    if (showLangDialog) {
        LanguageDialog(
            current = settings.language,
            onSelect = {
                vm.setLanguage(it)
                showLangDialog = false
            },
            onDismiss = { showLangDialog = false }
        )
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MapTypeDialog(
    current: Int,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val options = listOf(
        1 to stringResource(R.string.map_normal),
        2 to stringResource(R.string.map_satellite),
        3 to stringResource(R.string.map_terrain),
        4 to stringResource(R.string.map_hybrid)
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.please_select)) },
        text = {
            Column {
                options.forEach { (id, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(id) }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = current == id, onClick = { onSelect(id) })
                        Spacer(Modifier.width(8.dp))
                        Text(label)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.ok)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}

@Composable
private fun UnitSelectDialog(
    current: UnitMode,
    onSelect: (UnitMode) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.please_select)) },
        text = {
            Column {
                UnitMode.entries.forEach { mode ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(mode) }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = current == mode, onClick = { onSelect(mode) })
                        Spacer(Modifier.width(8.dp))
                        Text(mode.label)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.ok)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}

@Composable
private fun LanguageDialog(
    current: AppLanguage,
    onSelect: (AppLanguage) -> Unit,
    onDismiss: () -> Unit
) {
    val options = listOf(
        AppLanguage.SYSTEM to stringResource(R.string.lang_system),
        AppLanguage.ENGLISH to stringResource(R.string.lang_english),
        AppLanguage.INDONESIAN to stringResource(R.string.lang_indonesian)
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.please_select)) },
        text = {
            Column {
                options.forEach { (lang, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(lang) }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = current == lang, onClick = { onSelect(lang) })
                        Spacer(Modifier.width(8.dp))
                        Text(label)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.ok)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}


@Composable
private fun AboutPage(padding: PaddingValues, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
            }
            Text(stringResource(R.string.about_title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // App Information card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                ),
                shape = MaterialTheme.shapes.large
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        stringResource(R.string.about_app_info),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    AboutInfoRow(stringResource(R.string.about_app_name_label), stringResource(R.string.about_app_display_name))
                    AboutInfoRow(stringResource(R.string.about_version_label), stringResource(R.string.about_version_value))
                    AboutInfoRow(stringResource(R.string.about_build_label), stringResource(R.string.about_build_value))
                    AboutInfoRow(stringResource(R.string.about_platform_label), stringResource(R.string.about_platform_value))
                }
            }

            // Developer card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                ),
                shape = MaterialTheme.shapes.large
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(
                            stringResource(R.string.about_developer_name),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            stringResource(R.string.about_developer_role),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Features summary
            Text(stringResource(R.string.about_desc), style = MaterialTheme.typography.bodyMedium)
            Text(stringResource(R.string.about_features_title), fontWeight = FontWeight.SemiBold)
            Text(stringResource(R.string.about_f1))
            Text(stringResource(R.string.about_f2))
            Text(stringResource(R.string.about_f3))
            Text(stringResource(R.string.about_f4))
            Text(stringResource(R.string.about_f5))
            Text(stringResource(R.string.about_f6))
            Text(stringResource(R.string.about_f7))
            Text(stringResource(R.string.about_f8))
            Text(stringResource(R.string.about_f9))
            Text(stringResource(R.string.about_f10))
            Text(stringResource(R.string.about_local_data), style = MaterialTheme.typography.bodySmall)

            Spacer(Modifier.height(8.dp))
            AppFooter()
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun AboutInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun AppFooter() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        HorizontalDivider(modifier = Modifier.padding(horizontal = 48.dp, vertical = 8.dp))
        Text(
            stringResource(R.string.footer_app_name),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            stringResource(R.string.footer_copyright),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            stringResource(R.string.footer_made_with),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PrivacyPage(padding: PaddingValues, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
            }
            Text(stringResource(R.string.privacy_title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(stringResource(R.string.privacy_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(stringResource(R.string.privacy_updated))
            Text(stringResource(R.string.privacy_intro))
            Text(stringResource(R.string.privacy_s1), fontWeight = FontWeight.SemiBold)
            Text(stringResource(R.string.privacy_s1_a))
            Text(stringResource(R.string.privacy_s1_b))
            Text(stringResource(R.string.privacy_s1_c))
            Text(stringResource(R.string.privacy_s2), fontWeight = FontWeight.SemiBold)
            Text(stringResource(R.string.privacy_s2_body))
            Text(stringResource(R.string.privacy_s3), fontWeight = FontWeight.SemiBold)
            Text(stringResource(R.string.privacy_s3_body))
            Text(stringResource(R.string.privacy_s4), fontWeight = FontWeight.SemiBold)
            Text(stringResource(R.string.privacy_s4_body))
            Text(stringResource(R.string.privacy_s5), fontWeight = FontWeight.SemiBold)
            Text(stringResource(R.string.privacy_s5_body))
            Text(stringResource(R.string.privacy_s6), fontWeight = FontWeight.SemiBold)
            Text(stringResource(R.string.privacy_s6_body))
            Text(stringResource(R.string.privacy_s7), fontWeight = FontWeight.SemiBold)
            Text(stringResource(R.string.privacy_s7_body))
            Spacer(Modifier.height(16.dp))
            AppFooter()
            // end Privacy
        }
    }
}


@Composable
private fun LicensePage(padding: PaddingValues, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
            }
            Text(stringResource(R.string.license_title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(stringResource(R.string.license_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(stringResource(R.string.license_intro))
            Text(stringResource(R.string.license_s1), fontWeight = FontWeight.SemiBold)
            Text(stringResource(R.string.license_s1_body))
            Text(stringResource(R.string.license_s2), fontWeight = FontWeight.SemiBold)
            Text(stringResource(R.string.license_s2_body))
            Text(stringResource(R.string.license_s3), fontWeight = FontWeight.SemiBold)
            Text(stringResource(R.string.license_s3_body))
            Text(stringResource(R.string.license_s4), fontWeight = FontWeight.SemiBold)
            Text(stringResource(R.string.license_s4_body))
            Text(stringResource(R.string.license_s5), fontWeight = FontWeight.SemiBold)
            Text(stringResource(R.string.license_s5_body))
            Text(stringResource(R.string.license_s6), fontWeight = FontWeight.SemiBold)
            Text(stringResource(R.string.license_s6_body))
            Text(stringResource(R.string.license_s7), fontWeight = FontWeight.SemiBold)
            Text(stringResource(R.string.license_s7_body))
            Text(stringResource(R.string.license_s8), fontWeight = FontWeight.SemiBold)
            Text(stringResource(R.string.license_s8_body))
            Spacer(Modifier.height(16.dp))
            AppFooter()
            // end License
        }
    }
}

@Composable
private fun SecurityPage(padding: PaddingValues, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
            }
            Text(stringResource(R.string.security_title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(stringResource(R.string.security_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(stringResource(R.string.security_intro))
            Text(stringResource(R.string.security_s1), fontWeight = FontWeight.SemiBold)
            Text(stringResource(R.string.security_s1_body))
            Text(stringResource(R.string.security_s2), fontWeight = FontWeight.SemiBold)
            Text(stringResource(R.string.security_s2_body))
            Text(stringResource(R.string.security_s3), fontWeight = FontWeight.SemiBold)
            Text(stringResource(R.string.security_s3_body))
            Text(stringResource(R.string.security_s4), fontWeight = FontWeight.SemiBold)
            Text(stringResource(R.string.security_s4_body))
            Text(stringResource(R.string.security_s5), fontWeight = FontWeight.SemiBold)
            Text(stringResource(R.string.security_s5_body))
            Text(stringResource(R.string.security_s6), fontWeight = FontWeight.SemiBold)
            Text(stringResource(R.string.security_s6_body))
            Text(stringResource(R.string.security_s7), fontWeight = FontWeight.SemiBold)
            Text(stringResource(R.string.security_s7_body))
            Spacer(Modifier.height(16.dp))
            AppFooter()
            // end Security
        }
    }
}
