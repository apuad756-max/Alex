package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flare
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Swipe
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.AccessoryType
import com.example.data.models.BodyShape
import com.example.data.models.SkinEntity
import com.example.data.models.TrailType
import com.example.data.models.WeaponFx
import com.example.ui.GameViewModel
import com.example.ui.ScreenState
import com.example.ui.components.SkinPreviewBox

private val PALETTE_COLORS = listOf(
    0xFF00E5FF, // Neon Cyan
    0xFF7C4DFF, // Electric Purple
    0xFFFF1744, // Crimson Red
    0xFFFF9100, // Fiery Amber
    0xFF00E676, // Emerald Green
    0xFFFFD700, // Gold
    0xFFFF0055, // Vivid Pink
    0xFF00B0FF, // Frost Blue
    0xFFFFFFFF, // Pure White
    0xFFFFEA00  // Electric Yellow
)

@Composable
fun SkinCustomizerScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val draftSkin by viewModel.draftSkin.collectAsState()
    val allSkins by viewModel.allSkins.collectAsState()
    val equippedSkin by viewModel.equippedSkin.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Locker", "Gallery Asset", "Shape", "Aura Color", "Trail FX", "Accessory", "Weapons")

    // Image Picker Launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let { viewModel.importGalleryAsset(it) }
    }

    val fallbackContentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.importGalleryAsset(it) }
    }

    val auraColor = Color(draftSkin.auraColorHex)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(com.example.ui.theme.CleanMinimalBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Top Navigation Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.navigateTo(ScreenState.HOME) },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(com.example.ui.theme.CleanMinimalSurfaceVariant)
                    .border(1.dp, com.example.ui.theme.CleanMinimalBorderLight, CircleShape)
                    .testTag("customizer_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = com.example.ui.theme.CleanMinimalTextPrimary
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "CHARACTER STUDIO",
                style = TextStyle(
                    color = com.example.ui.theme.CleanMinimalTextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.5.sp
                )
            )
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = { viewModel.saveDraftAndEquip() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = com.example.ui.theme.CleanMinimalPrimary,
                    contentColor = com.example.ui.theme.CleanMinimalOnPrimary
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.testTag("save_equip_button")
            ) {
                Text(
                    text = "EQUIP",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Live Character Preview Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.CleanMinimalSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.CleanMinimalBorderLight)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SkinPreviewBox(
                    skin = draftSkin,
                    size = 120.dp,
                    radiusRatio = 0.32f
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "SKIN NAME",
                        fontSize = 10.sp,
                        color = com.example.ui.theme.CleanMinimalMuted,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = draftSkin.name,
                        onValueChange = { viewModel.updateDraftName(it) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = com.example.ui.theme.CleanMinimalPrimary,
                            unfocusedBorderColor = com.example.ui.theme.CleanMinimalBorder,
                            focusedTextColor = com.example.ui.theme.CleanMinimalTextPrimary,
                            unfocusedTextColor = com.example.ui.theme.CleanMinimalTextPrimary,
                            cursorColor = com.example.ui.theme.CleanMinimalPrimary,
                            focusedContainerColor = com.example.ui.theme.CleanMinimalBackground,
                            unfocusedContainerColor = com.example.ui.theme.CleanMinimalBackground
                        ),
                        textStyle = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("skin_name_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(auraColor)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (draftSkin.isCustomGallery) "Gallery Sprite" else draftSkin.bodyShape.displayName,
                            color = com.example.ui.theme.CleanMinimalTextSecondary,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Customization Tab Selector
        ScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = com.example.ui.theme.CleanMinimalBackground,
            contentColor = com.example.ui.theme.CleanMinimalTextPrimary,
            edgePadding = 16.dp,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = com.example.ui.theme.CleanMinimalPrimary,
                    height = 3.dp
                )
            },
            divider = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(com.example.ui.theme.CleanMinimalBorderLight)
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = {
                        selectedTabIndex = index
                        viewModel.soundSynth.playClick()
                    },
                    text = {
                        Text(
                            text = title.uppercase(),
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTabIndex == index) com.example.ui.theme.CleanMinimalPrimary else com.example.ui.theme.CleanMinimalMuted
                        )
                    }
                )
            }
        }

        // Tab Content Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(16.dp)
        ) {
            when (selectedTabIndex) {
                0 -> LockerTab(
                    allSkins = allSkins,
                    equippedSkin = equippedSkin,
                    onSelectSkin = { viewModel.startCustomizing(it) },
                    onEquip = { viewModel.equipSkin(it.id) },
                    onDelete = { viewModel.deleteSkin(it.id) },
                    onNewGallerySkin = {
                        try {
                            photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        } catch (e: Exception) {
                            fallbackContentLauncher.launch("image/*")
                        }
                    }
                )
                1 -> GalleryImportTab(
                    isCustom = draftSkin.isCustomGallery,
                    onPickImage = {
                        try {
                            photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        } catch (e: Exception) {
                            fallbackContentLauncher.launch("image/*")
                        }
                    }
                )
                2 -> ShapeTab(
                    selected = draftSkin.bodyShape,
                    onSelect = { viewModel.updateDraftBodyShape(it) }
                )
                3 -> AuraColorTab(
                    primaryColorHex = draftSkin.auraColorHex,
                    secondaryColorHex = draftSkin.secondaryColorHex,
                    onSelectPrimary = { viewModel.updateDraftAuraColor(it) },
                    onSelectSecondary = { viewModel.updateDraftSecondaryColor(it) }
                )
                4 -> TrailTab(
                    selected = draftSkin.trailType,
                    onSelect = { viewModel.updateDraftTrail(it) }
                )
                5 -> AccessoryTab(
                    selected = draftSkin.accessory,
                    onSelect = { viewModel.updateDraftAccessory(it) }
                )
                6 -> WeaponTab(
                    selected = draftSkin.weaponFx,
                    onSelect = { viewModel.updateDraftWeapon(it) }
                )
            }
        }
    }
}

@Composable
private fun LockerTab(
    allSkins: List<SkinEntity>,
    equippedSkin: SkinEntity?,
    onSelectSkin: (SkinEntity) -> Unit,
    onEquip: (SkinEntity) -> Unit,
    onDelete: (SkinEntity) -> Unit,
    onNewGallerySkin: () -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            // Import new skin banner
            Card(
                onClick = onNewGallerySkin,
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.CleanMinimalContainer),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, com.example.ui.theme.CleanMinimalPrimary.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                    .testTag("import_gallery_banner")
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.8f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = "Import",
                            tint = com.example.ui.theme.CleanMinimalPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "IMPORT FROM GALLERY",
                            color = com.example.ui.theme.CleanMinimalPrimaryDark,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Pick any image to create a custom skin",
                            color = com.example.ui.theme.CleanMinimalTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        items(allSkins, key = { it.id }) { skin ->
            val isEquipped = skin.id == equippedSkin?.id
            val skinAura = Color(skin.auraColorHex)

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isEquipped) com.example.ui.theme.CleanMinimalContainer else com.example.ui.theme.CleanMinimalSurface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = if (isEquipped) 1.5.dp else 1.dp,
                        color = if (isEquipped) com.example.ui.theme.CleanMinimalPrimary else com.example.ui.theme.CleanMinimalBorderLight,
                        shape = RoundedCornerShape(20.dp)
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SkinPreviewBox(
                        skin = skin,
                        size = 56.dp,
                        radiusRatio = 0.30f
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = skin.name,
                                color = com.example.ui.theme.CleanMinimalTextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            if (skin.isCustomGallery) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(com.example.ui.theme.CleanMinimalSurfaceVariant)
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "GALLERY",
                                        color = com.example.ui.theme.CleanMinimalPrimary,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                        Text(
                            text = "${skin.bodyShape.displayName} • ${skin.trailType.displayName}",
                            color = com.example.ui.theme.CleanMinimalTextSecondary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // Edit Button
                    IconButton(onClick = { onSelectSkin(skin) }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Skin",
                            tint = com.example.ui.theme.CleanMinimalMuted
                        )
                    }

                    // Equip Button
                    if (isEquipped) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(com.example.ui.theme.CleanMinimalSurfaceVariant)
                                .border(1.dp, com.example.ui.theme.CleanMinimalPrimary, RoundedCornerShape(12.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "ACTIVE",
                                color = com.example.ui.theme.CleanMinimalPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    } else {
                        Button(
                            onClick = { onEquip(skin) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = com.example.ui.theme.CleanMinimalSurfaceVariant,
                                contentColor = com.example.ui.theme.CleanMinimalTextPrimary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "EQUIP",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // Delete custom skin
                    if (skin.id.startsWith("custom_")) {
                        IconButton(onClick = { onDelete(skin) }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = Color(0xFFB3261E)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GalleryImportTab(
    isCustom: Boolean,
    onPickImage: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(com.example.ui.theme.CleanMinimalContainer)
                .border(1.5.dp, com.example.ui.theme.CleanMinimalPrimary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PhotoLibrary,
                contentDescription = "Gallery",
                tint = com.example.ui.theme.CleanMinimalPrimary,
                modifier = Modifier.size(44.dp)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "CUSTOM GALLERY SKIN",
            color = com.example.ui.theme.CleanMinimalTextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Select any photo, character art, or sprite from your gallery. It will be mapped directly onto your hero with dynamic aura and particles!",
            color = com.example.ui.theme.CleanMinimalTextSecondary,
            fontSize = 13.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onPickImage,
            colors = ButtonDefaults.buttonColors(
                containerColor = com.example.ui.theme.CleanMinimalPrimary,
                contentColor = com.example.ui.theme.CleanMinimalOnPrimary
            ),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.testTag("pick_gallery_image_button")
        ) {
            Icon(imageVector = Icons.Default.AddPhotoAlternate, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "CHOOSE PHOTO FROM GALLERY",
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun ShapeTab(
    selected: BodyShape,
    onSelect: (BodyShape) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(BodyShape.values()) { shape ->
            val isSelected = selected == shape
            Card(
                onClick = { onSelect(shape) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) com.example.ui.theme.CleanMinimalContainer else com.example.ui.theme.CleanMinimalSurface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) com.example.ui.theme.CleanMinimalPrimary else com.example.ui.theme.CleanMinimalBorderLight,
                        shape = RoundedCornerShape(16.dp)
                    )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = shape.displayName,
                        color = com.example.ui.theme.CleanMinimalTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f)
                    )
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = com.example.ui.theme.CleanMinimalPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AuraColorTab(
    primaryColorHex: Long,
    secondaryColorHex: Long,
    onSelectPrimary: (Long) -> Unit,
    onSelectSecondary: (Long) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = "PRIMARY AURA GLOW",
                color = com.example.ui.theme.CleanMinimalTextSecondary,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PALETTE_COLORS.forEach { colorHex ->
                    val color = Color(colorHex)
                    val isSelected = primaryColorHex == colorHex
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (isSelected) 3.5.dp else 1.dp,
                                color = if (isSelected) com.example.ui.theme.CleanMinimalPrimary else com.example.ui.theme.CleanMinimalBorder,
                                shape = CircleShape
                            )
                            .clickable { onSelectPrimary(colorHex) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = if (colorHex == 0xFFFFFFFFL || colorHex == 0xFFFFEA00L) Color.Black else Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "SECONDARY ACCENT COLOR",
                color = com.example.ui.theme.CleanMinimalTextSecondary,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PALETTE_COLORS.reversed().forEach { colorHex ->
                    val color = Color(colorHex)
                    val isSelected = secondaryColorHex == colorHex
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (isSelected) 3.5.dp else 1.dp,
                                color = if (isSelected) com.example.ui.theme.CleanMinimalPrimary else com.example.ui.theme.CleanMinimalBorder,
                                shape = CircleShape
                            )
                            .clickable { onSelectSecondary(colorHex) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = if (colorHex == 0xFFFFFFFFL || colorHex == 0xFFFFEA00L) Color.Black else Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TrailTab(
    selected: TrailType,
    onSelect: (TrailType) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(TrailType.values()) { trail ->
            val isSelected = selected == trail
            Card(
                onClick = { onSelect(trail) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) com.example.ui.theme.CleanMinimalContainer else com.example.ui.theme.CleanMinimalSurface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) com.example.ui.theme.CleanMinimalPrimary else com.example.ui.theme.CleanMinimalBorderLight,
                        shape = RoundedCornerShape(16.dp)
                    )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Grain,
                        contentDescription = null,
                        tint = if (isSelected) com.example.ui.theme.CleanMinimalPrimary else com.example.ui.theme.CleanMinimalMuted
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = trail.displayName,
                        color = com.example.ui.theme.CleanMinimalTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f)
                    )
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = com.example.ui.theme.CleanMinimalPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AccessoryTab(
    selected: AccessoryType,
    onSelect: (AccessoryType) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(AccessoryType.values()) { acc ->
            val isSelected = selected == acc
            Card(
                onClick = { onSelect(acc) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) com.example.ui.theme.CleanMinimalContainer else com.example.ui.theme.CleanMinimalSurface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) com.example.ui.theme.CleanMinimalPrimary else com.example.ui.theme.CleanMinimalBorderLight,
                        shape = RoundedCornerShape(16.dp)
                    )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = when (acc) {
                            AccessoryType.CYBER_VISOR -> Icons.Default.Visibility
                            AccessoryType.NEON_CROWN -> Icons.Default.Stars
                            AccessoryType.ORBITING_ORBS -> Icons.Default.Flare
                            AccessoryType.ANGEL_WINGS -> Icons.Default.Shield
                            else -> Icons.Default.ColorLens
                        },
                        contentDescription = null,
                        tint = if (isSelected) com.example.ui.theme.CleanMinimalPrimary else com.example.ui.theme.CleanMinimalMuted
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = acc.displayName,
                        color = com.example.ui.theme.CleanMinimalTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f)
                    )
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = com.example.ui.theme.CleanMinimalPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WeaponTab(
    selected: WeaponFx,
    onSelect: (WeaponFx) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(WeaponFx.values()) { weapon ->
            val isSelected = selected == weapon
            Card(
                onClick = { onSelect(weapon) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) com.example.ui.theme.CleanMinimalContainer else com.example.ui.theme.CleanMinimalSurface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) com.example.ui.theme.CleanMinimalPrimary else com.example.ui.theme.CleanMinimalBorderLight,
                        shape = RoundedCornerShape(16.dp)
                    )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Swipe,
                        contentDescription = null,
                        tint = if (isSelected) com.example.ui.theme.CleanMinimalPrimary else com.example.ui.theme.CleanMinimalMuted
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = weapon.displayName,
                        color = com.example.ui.theme.CleanMinimalTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f)
                    )
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = com.example.ui.theme.CleanMinimalPrimary
                        )
                    }
                }
            }
        }
    }
}
