package com.ensiie.justine.parcoursup.ui.detail


import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.core.net.toUri
import com.ensiie.justine.parcoursup.R
import com.ensiie.justine.parcoursup.data.model.EtablissementContactInfo
import com.ensiie.justine.parcoursup.data.model.Formation
import com.ensiie.justine.parcoursup.ui.theme.dsfr.DSFRTheme
import com.ensiie.justine.parcoursup.ui.theme.dsfr.components.DsfrCard
import com.ensiie.justine.parcoursup.ui.theme.dsfr.components.DsfrCircularProgressIndicator
import com.ensiie.justine.parcoursup.ui.theme.dsfr.components.DsfrLinearProgressIndicator
import com.ensiie.justine.parcoursup.ui.theme.dsfr.components.DsfrTag
import com.ensiie.justine.parcoursup.ui.theme.dsfr.components.DsfrTagSize
import com.ensiie.justine.parcoursup.ui.theme.dsfr.components.DsfrTagType
import com.ensiie.justine.parcoursup.ui.theme.dsfr.components.DsfrText
import com.ensiie.justine.parcoursup.ui.theme.dsfr.components.DsfrTextColor
import com.ensiie.justine.parcoursup.ui.theme.dsfr.components.DsfrTextStyle
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonPrimitive
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.expressions.dsl.asString
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.expressions.dsl.feature
import org.maplibre.compose.expressions.dsl.image
import org.maplibre.compose.expressions.dsl.offset
import org.maplibre.compose.layers.SymbolLayer
import org.maplibre.compose.map.GestureOptions
import org.maplibre.compose.map.MapOptions
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.map.OrnamentOptions
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.GeoJsonOptions
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.style.BaseStyle
import org.maplibre.compose.style.rememberStyleState
import org.maplibre.compose.util.ClickResult
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.geojson.toJson


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormationDetailScreen(
    id: String,
    onNavigateBack: () -> Unit,
    onNavigateToFormation: (String) -> Unit = {},
    viewModel: FormationDetailViewModel = koinViewModel(key = id) { parametersOf(id) }
) {
    val uiState by viewModel.uiState.collectAsState()
    val isFavorite by viewModel.isFavorite.collectAsState(initial = false)

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = DSFRTheme.colors.backgroundRaised,
                titleContentColor = DSFRTheme.colors.textDefault,
                navigationIconContentColor = DSFRTheme.colors.textDefault,
                actionIconContentColor = DSFRTheme.colors.textDefault
            ),
            windowInsets = WindowInsets(0.dp),
            title = {
                DsfrText(
                    stringResource(R.string.details),
                    textStyle = DsfrTextStyle.HeadingLg
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back)
                    )
                }
            },
            actions = {
                if (uiState is DetailUiState.Success) {
                    IconButton(onClick = { viewModel.toggleFavorite() }) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite
                            else Icons.Filled.FavoriteBorder,
                            contentDescription = stringResource(
                                if (isFavorite) R.string.remove_from_favorites
                                else R.string.add_to_favorites
                            ),
                            tint = if (isFavorite) DSFRTheme.colors.error
                            else DSFRTheme.colors.textDefault
                        )
                    }
                }
            }
        )

        when (val state = uiState) {
            is DetailUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    DsfrCircularProgressIndicator()
                }
            }

            is DetailUiState.Success -> {
                FormationDetailContent(
                    formation = state.formation,
                    etablissementInfo = state.etablissementInfo,
                    similarFormations = state.similarFormations,
                    onSimilarFormationClick = { formationId ->
                        onNavigateToFormation(formationId)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            is DetailUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(DSFRTheme.spacing.md)
                    ) {
                        DsfrText(
                            text = state.message,
                            color = DsfrTextColor.Error
                        )
                        Button(onClick = { viewModel.retry() }) {
                            DsfrText(stringResource(R.string.retry))
                        }
                    }
                }
            }
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun FormationDetailContent(
    formation: Formation,
    etablissementInfo: EtablissementContactInfo,
    similarFormations: List<Formation> = emptyList(),
    onSimilarFormationClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(state = rememberScrollState())
            .padding(DSFRTheme.spacing.md),
        verticalArrangement = Arrangement.spacedBy(DSFRTheme.spacing.md),
    ) {
        DsfrCard(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(DSFRTheme.spacing.md),
                verticalArrangement = Arrangement.spacedBy(DSFRTheme.spacing.sm)
            ) {
                DsfrText(
                    text = formation.etablissement
                        ?: stringResource(R.string.establishment_unknown),
                    textStyle = DsfrTextStyle.HeadingXl
                )
                DsfrText(
                    text = formation.formationComplete
                        ?: stringResource(R.string.formation),
                    textStyle = DsfrTextStyle.HeadingMd,
                )
                DsfrText(
                    text = "${formation.ville ?: ""} - ${formation.departement ?: ""}",
                    textStyle = DsfrTextStyle.BodyMd,
                    color = DsfrTextColor.Label
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DsfrTag(
                        text = formation.filiere ?: "",
                        type = DsfrTagType.Primary,
                        size = DsfrTagSize.Small
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(DSFRTheme.spacing.md)
                    ) {
                        if (!etablissementInfo.telephone.isNullOrEmpty()) {
                            ContactButton(
                                icon = painterResource(R.drawable.ic_phone),
                                contentDescription = "Téléphone",
                                value = etablissementInfo.telephone,
                                onClickAction = {
                                    val intent = Intent(
                                        Intent.ACTION_DIAL,
                                        "tel:${etablissementInfo.telephone}".toUri()
                                    )
                                    context.startActivity(intent)
                                }
                            )
                        }

                        if (!etablissementInfo.mail.isNullOrEmpty()) {
                            ContactButton(
                                icon = painterResource(R.drawable.ic_mail),
                                contentDescription = "Email",
                                value = etablissementInfo.mail,
                                onClickAction = {
                                    val intent = Intent(
                                        Intent.ACTION_SENDTO,
                                        "mailto:${etablissementInfo.mail}".toUri()
                                    )
                                    context.startActivity(intent)
                                }
                            )
                        }

                        if (!etablissementInfo.siteWeb.isNullOrEmpty()) {
                            ContactButton(
                                icon = painterResource(R.drawable.ic_web),
                                contentDescription = "Site Web",
                                value = etablissementInfo.siteWeb,
                                onClickAction = {
                                    val intent = Intent(
                                        Intent.ACTION_VIEW,
                                        etablissementInfo.siteWeb.toUri()
                                    )
                                    context.startActivity(intent)
                                }
                            )
                        }
                    }
                }
            }
        }

        DsfrText(
            text = stringResource(R.string.stats_2024),
            textStyle = DsfrTextStyle.HeadingMd,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(DSFRTheme.spacing.sm)
        ) {
            StatCard(
                title = stringResource(R.string.capacity),
                value = formation.capacite?.toString() ?: stringResource(R.string.not_available),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = stringResource(R.string.wishes),
                value = formation.voeux?.toString() ?: stringResource(R.string.not_available),
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(DSFRTheme.spacing.sm)
        ) {
            StatCard(
                title = stringResource(R.string.admitted),
                value = formation.admis?.toString() ?: stringResource(R.string.not_available),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = stringResource(R.string.access_rate),
                value = formation.tauxAcces?.let {
                    String.format("%.1f%%", it)
                } ?: stringResource(R.string.not_available),
                modifier = Modifier.weight(1f)
            )
        }

        DsfrText(
            text = "Statistiques de répartition",
            textStyle = DsfrTextStyle.HeadingMd,
        )

        val tabs = listOf(
            stringResource(R.string.gender_distribution),
            stringResource(R.string.bac_distribution),
            stringResource(R.string.bac_mentions)
        )
        val pagerState = rememberPagerState(pageCount = { tabs.size })
        val coroutineScope = rememberCoroutineScope()

        SecondaryScrollableTabRow(
            selectedTabIndex = pagerState.currentPage,
            containerColor = DSFRTheme.colors.backgroundDefault,
            contentColor = DSFRTheme.colors.textDefault,
            edgePadding = 0.dp
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    text = {
                        DsfrText(
                            text = title,
                            textStyle = DsfrTextStyle.BodyMd,
                            color = if (pagerState.currentPage == index) DsfrTextColor.Primary else DsfrTextColor.Mention
                        )
                    }
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            DsfrCard(modifier = Modifier.fillMaxWidth()) {
                when (page) {
                    0 -> {
                        Column(
                            modifier = Modifier.padding(DSFRTheme.spacing.md),
                            verticalArrangement = Arrangement.spacedBy(DSFRTheme.spacing.sm)
                        ) {
                            ProgressRow(
                                label = stringResource(R.string.women),
                                percentage = formation.pourcentageFemmes ?: 0.0,
                                count = formation.admisFemmes ?: 0
                            )
                            ProgressRow(
                                label = stringResource(R.string.men),
                                percentage = 100 - (formation.pourcentageFemmes ?: 0.0),
                                count = (formation.admis ?: 0) - (formation.admisFemmes ?: 0)
                            )
                        }
                    }

                    1 -> {
                        Column(
                            modifier = Modifier.padding(DSFRTheme.spacing.md),
                            verticalArrangement = Arrangement.spacedBy(DSFRTheme.spacing.sm)
                        ) {
                            ProgressRow(
                                label = stringResource(R.string.bac_general),
                                percentage = formation.pourcentageGenerale ?: 0.0,
                                count = formation.admisGenerale ?: 0
                            )
                            ProgressRow(
                                label = stringResource(R.string.bac_tech),
                                percentage = formation.pourcentageTechnologique ?: 0.0,
                                count = formation.admisTechnologique ?: 0
                            )
                            ProgressRow(
                                label = stringResource(R.string.bac_pro),
                                percentage = formation.pourcentageProfessionnel ?: 0.0,
                                count = formation.admisProfessionnel ?: 0
                            )
                        }
                    }

                    2 -> {
                        Column(
                            modifier = Modifier.padding(DSFRTheme.spacing.md),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            MentionRow(
                                stringResource(R.string.mention_none),
                                formation.mentionSans ?: 0
                            )
                            MentionRow(
                                stringResource(R.string.mention_ab),
                                formation.mentionAB ?: 0
                            )
                            MentionRow(stringResource(R.string.mention_b), formation.mentionB ?: 0)
                            MentionRow(
                                stringResource(R.string.mention_tb),
                                formation.mentionTB ?: 0
                            )
                            MentionRow(
                                stringResource(R.string.mention_tbf),
                                formation.mentionTBF?.toInt() ?: 0
                            )
                        }
                    }
                }
            }
        }

        if (similarFormations.isNotEmpty()) {
            DsfrText(
                text = stringResource(R.string.similar_formations),
                textStyle = DsfrTextStyle.HeadingMd
            )

            val similarPagerState =
                rememberPagerState(pageCount = { similarFormations.take(5).size })

            HorizontalPager(
                state = similarPagerState,
                modifier = Modifier.fillMaxWidth(),
                pageSpacing = DSFRTheme.spacing.sm,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = DSFRTheme.spacing.md),
                beyondViewportPageCount = 1
            ) { page ->
                val similarFormation = similarFormations[page]
                SimilarFormationCard(
                    formation = similarFormation,
                    onClick = { onSimilarFormationClick(similarFormation.id) }
                )
            }
        }

        val coords = formation.coordonnees
        if (coords != null && coords.size >= 2) {
            DsfrText(
                text = stringResource(R.string.location),
                textStyle = DsfrTextStyle.HeadingMd
            )
            DsfrCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                FormationMap(
                    latitude = coords[0],
                    longitude = coords[1],
                    etablissement = formation.etablissement ?: "",
                    onMarkerClick = {
                        val latitude = coords[0]
                        val longitude = coords[1]
                        val label = formation.etablissement ?: "Formation Location"
                        val gmmIntentUri =
                            "geo:$latitude,$longitude?q=$latitude,$longitude($label)".toUri()
                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                        context.startActivity(mapIntent)
                    }
                )
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    DsfrCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(DSFRTheme.spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            DsfrText(
                text = title,
                textStyle = DsfrTextStyle.BodySm,
                color = DsfrTextColor.Label
            )
            DsfrText(
                text = value,
                textStyle = DsfrTextStyle.DisplayMd
            )
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun ProgressRow(
    label: String,
    percentage: Double,
    count: Int
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            DsfrText(
                text = label,
                textStyle = DsfrTextStyle.BodySm
            )
            DsfrText(
                text = "${String.format("%.1f", percentage)}% ($count)",
                textStyle = DsfrTextStyle.BodySm
            )
        }
        DsfrLinearProgressIndicator(
            progress = { (percentage / 100).toFloat() },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun MentionRow(label: String, count: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        DsfrText(text = label, textStyle = DsfrTextStyle.BodyMd)
        DsfrText(text = count.toString(), textStyle = DsfrTextStyle.BodyMd)
    }
}

@Composable
fun FormationMap(
    latitude: Double,
    longitude: Double,
    etablissement: String,
    onMarkerClick: () -> Unit = {}
) {
    val cameraState = rememberCameraState(
        CameraPosition(target = Position(longitude, latitude), zoom = 15.0)
    )
    val marker = painterResource(R.drawable.marker)
    LocalContext.current

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        MaplibreMap(
            baseStyle = BaseStyle.Uri("https://tiles.openfreemap.org/styles/liberty"),
            modifier = Modifier.fillMaxSize(),
            cameraState = cameraState,
            styleState = rememberStyleState(),
            onMapClick = { point, screenPoint ->
                ClickResult.Pass
            },
            options =
                MapOptions(
                    ornamentOptions = OrnamentOptions.AllDisabled,
                    gestureOptions =
                        GestureOptions(
                            isTiltEnabled = false,
                            isZoomEnabled = false,
                            isRotateEnabled = false,
                            isScrollEnabled = false,
                        ),
                ),
        ) {

            val feat = Feature(
                geometry = Point(longitude = longitude, latitude = latitude),
                properties = mapOf("title" to JsonPrimitive(etablissement))
            )
            val fc = FeatureCollection(listOf(feat))
            val source = rememberGeoJsonSource(
                GeoJsonData.JsonString(fc.toJson()),
                GeoJsonOptions()
            )

            // Marker icon
            SymbolLayer(
                id = "marker-icon",
                source = source,
                iconImage = image(marker),
                iconAllowOverlap = const(true),
                textField = feature["title"].asString(),
                textFont = const(listOf("Noto Sans Regular")),
                textColor = const(androidx.compose.ui.graphics.Color.Black),
                textOffset = offset(0.em, 0.6.em),
                onClick = { point ->
                    onMarkerClick()
                    return@SymbolLayer ClickResult.Consume
                }
            )
        }
    }
}

@Composable
fun ContactButton(
    icon: Painter,
    contentDescription: String,
    value: String,
    onClickAction: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    Box(
        modifier = Modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClickAction() },
                    onLongPress = {
                        val clip = ClipData.newPlainText(contentDescription, value)
                        clipboardManager.setPrimaryClip(clip)
                        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2)
                            Toast.makeText(context, "$contentDescription copié", Toast.LENGTH_SHORT)
                                .show()
                    }
                )
            }
            .size(DSFRTheme.spacing.md),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = icon,
            contentDescription = contentDescription,
            tint = DSFRTheme.colors.actionHigh,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun SimilarFormationCard(
    formation: Formation,
    onClick: () -> Unit
) {
    DsfrCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DSFRTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(DSFRTheme.spacing.xs)
        ) {
            DsfrText(
                text = formation.etablissement
                    ?: stringResource(R.string.establishment_unknown),
                textStyle = DsfrTextStyle.HeadingMd
            )
            DsfrText(
                text = formation.formationComplete
                    ?: stringResource(R.string.formation),
                textStyle = DsfrTextStyle.BodySm,
                color = DsfrTextColor.Mention
            )
            DsfrText(
                text = "${formation.ville ?: ""} - ${formation.departement ?: ""}",
                textStyle = DsfrTextStyle.BodyXs,
                color = DsfrTextColor.Label
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(DSFRTheme.spacing.sm)
            ) {
                formation.capacite?.let {
                    DsfrTag(
                        stringResource(R.string.capacity) + ": $it",
                        type = DsfrTagType.Primary,
                        size = DsfrTagSize.Small
                    )
                }
                formation.tauxAcces?.let {
                    DsfrTag(
                        stringResource(R.string.access_format, it),
                        type = DsfrTagType.Primary,
                        size = DsfrTagSize.Small
                    )
                }
            }
        }
    }
}