package com.notistorex.app.ui.screens.dashboard

import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.notistorex.app.R
import com.notistorex.app.ads.PurchaseState
import com.notistorex.app.ui.screens.dashboard.viewmodel.PremiumViewModel
import com.notistorex.app.ui.theme.JetpackComposeTest1Theme
import com.notistorex.app.ui.theme.main_appColor
import com.notistorex.app.utils.BillingConstants
import kotlinx.coroutines.launch
import java.time.Period

@Composable
fun AdFreeScreenView(
    onNavigateBack: () -> Unit,
    premiumViewModel: PremiumViewModel = hiltViewModel()
) {
    val isPremium by premiumViewModel.isPremium.collectAsState()
    val purchaseState by premiumViewModel.purchaseState.collectAsState()
    val subscriptionPlans by premiumViewModel.subscriptionPlans.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val planOptions = remember(subscriptionPlans) {
        subscriptionPlans.map { plan ->
            SubscriptionPlanOption(
                productId = plan.productId,
                basePlanId = plan.basePlanId,
                offerToken = plan.offerToken,
                title = plan.basePlanId.formatBasePlanTitle(),
                subtitle = formatBillingPeriod(plan.billingPeriod),
                price = plan.price,
                rawBillingPeriod = plan.billingPeriod
            )
        }
    }

    var selectedPlanToken by rememberSaveable(planOptions) {
        mutableStateOf(planOptions.firstOrNull()?.offerToken.orEmpty())
    }

    LaunchedEffect(planOptions) {
        if (planOptions.none { it.offerToken == selectedPlanToken }) {
            selectedPlanToken = planOptions.firstOrNull()?.offerToken.orEmpty()
        }
    }

    LaunchedEffect(purchaseState) {
        when (purchaseState) {
            is PurchaseState.Success -> {
                snackbarHostState.showSnackbar(context.getString(R.string.purchase_successful))
                premiumViewModel.refreshPurchases()
            }
            is PurchaseState.Error -> {
                val error = (purchaseState as PurchaseState.Error).message
                snackbarHostState.showSnackbar(context.getString(R.string.purchase_failed,"$error"))
            }
            is PurchaseState.Cancelled -> {
                snackbarHostState.showSnackbar(context.getString(R.string.purchaseCancelled))
            }
            is PurchaseState.AlreadyOwned -> {
                snackbarHostState.showSnackbar(context.getString(R.string.your_already_own_this_subscription))
                premiumViewModel.refreshPurchases()
            }
            else -> Unit
        }
    }

    AdFreeScreenContent(
        onNavigateBack = onNavigateBack,
        isPremium = isPremium,
        isLoading = purchaseState is PurchaseState.Processing,
        availablePlans = planOptions,
        selectedPlanToken = selectedPlanToken,
        onPlanSelected = { selectedPlanToken = it },
        onPurchase = { plan ->
            val productId = plan.productId
            if (activity != null && plan.offerToken.isNotBlank()) {
                premiumViewModel.purchaseSubscription(activity, productId, plan.offerToken)
            } else {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(context.getString(R.string.unable_toStart_purchase))
                }
            }
        },
        onSelectFreePlan = { premiumViewModel.setPremium(false) },
        snackbarHostState = snackbarHostState
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdFreeScreenContent(
    onNavigateBack: () -> Unit,
    isPremium: Boolean,
    isLoading: Boolean = false,
    availablePlans: List<SubscriptionPlanOption>,
    selectedPlanToken: String,
    onPlanSelected: (String) -> Unit,
    onPurchase: (SubscriptionPlanOption) -> Unit,
    onSelectFreePlan: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = stringResource(id = R.string.ad_free), fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.back),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        val featureAccent = main_appColor
        val fallbackPlans = listOf(
            SubscriptionPlanOption(
                productId = BillingConstants.PREMIUM_PRODUCT_ID,
                basePlanId = "default_monthly",
                offerToken = "",
                title = stringResource(id = R.string.ad_free_plan_monthly),
                subtitle = stringResource(id = R.string.ad_free_plan_monthly_subtitle),
                price = stringResource(id = R.string.ad_free_plan_monthly_price),
                rawBillingPeriod = null
            )
        )
        val plans = availablePlans.takeIf { it.isNotEmpty() } ?: fallbackPlans
        val selectedPlan = plans.firstOrNull { it.offerToken == selectedPlanToken }
            ?: plans.firstOrNull()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            if (isPremium) {
                ActiveSubscriptionBanner()
            }

            Text(
                text = stringResource(id = R.string.ad_free_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f)
            )

            FeatureHighlightCard(accentColor = featureAccent)

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                plans.forEach { plan ->
                    PlanCard(
                        plan = plan,
                        selected = plan.offerToken == selectedPlan?.offerToken,
                        accentColor = featureAccent,
                        onClick = { onPlanSelected(plan.offerToken) }
                    )
                }
            }

            Text(
                text = stringResource(id = R.string.ad_free_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { selectedPlan?.let(onPurchase) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading && !isPremium && selectedPlan?.offerToken?.isNotBlank() == true,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = featureAccent,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                    }
                    val buttonText = selectedPlan?.title ?: stringResource(id = R.string.ad_free_plan_monthly)
                    Text(
                        text = if (isLoading) "Processing..." else stringResource(
                            id = R.string.ad_free_continue_button,
                            buttonText
                        ),
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                OutlinedButton(
                    onClick = onSelectFreePlan,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading && !isPremium,
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.ad_free_free_plan_button),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun FeatureHighlightCard(
    accentColor: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        accentColor,
                        accentColor.copy(alpha = 0.9f),
                        accentColor.copy(alpha = 0.8f)
                    )
                )
            )
            .padding(horizontal = 24.dp, vertical = 28.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = stringResource(id = R.string.ad_free_feature_list_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            FeatureRow(text = stringResource(id = R.string.weekly_app_breakdown))
            FeatureRow(text = stringResource(id = R.string.export_all_data))
            FeatureRow(text = stringResource(id = R.string.storage_auto_cleanup))
            FeatureRow(text = stringResource(id = R.string.ad_free_feature_ad_removal))
        }
    }
}

@Composable
private fun FeatureRow(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = Color.White
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = Color.White
        )
    }
}

@Composable
private fun PlanCard(
    plan: SubscriptionPlanOption,
    selected: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    val borderColor = if (selected) accentColor else MaterialTheme.colorScheme.outlineVariant
    val backgroundColor = if (selected) {
        accentColor.copy(alpha = 0.12f)
    } else {
        MaterialTheme.colorScheme.surface
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(width = if (selected) 2.dp else 1.dp, color = borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = plan.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = plan.price,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                    plan.originalPrice?.takeIf { it.isNotBlank() }?.let { original ->
                        Text(
                            text = original,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textDecoration = TextDecoration.LineThrough,
                            modifier = Modifier.padding(start = 4.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
                Text(
                    text = plan.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                plan.discount?.takeIf { it.isNotBlank() }?.let { discount ->
                    Text(
                        text = discount,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(accentColor)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
                Icon(
                    imageVector = if (selected) Icons.Filled.RadioButtonChecked else Icons.Filled.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = accentColor
                )
            }
        }
    }
}

private data class SubscriptionPlanOption(
    val productId: String,
    val basePlanId: String,
    val offerToken: String,
    val title: String,
    val subtitle: String,
    val price: String,
    val discount: String? = null,
    val originalPrice: String? = null,
    val rawBillingPeriod: String?
)

private fun String.formatBasePlanTitle(): String {
    val normalized = lowercase()
    return when {
        normalized.contains("month") || normalized.contains("p1m") -> "Monthly"
        normalized.contains("quarter") || normalized.contains("p3m") -> "Quarterly"
        normalized.contains("6") && normalized.contains("m") -> "6 Months"
        normalized.contains("year") || normalized.contains("p1y") -> "Yearly"
        else -> replace('_', ' ').replaceFirstChar { it.uppercase() }
    }
}

private fun formatBillingPeriod(isoPeriod: String): String {
    return try {
        val period = Period.parse(isoPeriod)
        when {
            period.years > 0 -> "Billed every ${period.years} year${if (period.years > 1) "s" else ""}"
            period.months > 0 -> "Billed every ${period.months} month${if (period.months > 1) "s" else ""}"
            period.days > 0 -> "Billed every ${period.days} day${if (period.days > 1) "s" else ""}"
            else -> "Flexible billing"
        }
    } catch (e: Exception) {
        when {
            isoPeriod.endsWith("Y") -> "Billed yearly"
            isoPeriod.endsWith("M") -> "Billed monthly"
            else -> "Flexible billing"
        }
    }
}

@Composable
private fun ActiveSubscriptionBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(main_appColor.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = main_appColor
            )
        }
        Column {
            Text(
                text = stringResource(id = R.string.premium_active_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(id = R.string.premium_active_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview
@Composable
private fun AdFreeScreenPreview() {
    JetpackComposeTest1Theme {
        AdFreeScreenContent(
            onNavigateBack = {},
            isPremium = false,
            isLoading = false,
            availablePlans = listOf(
                SubscriptionPlanOption(
                    productId = BillingConstants.PREMIUM_PRODUCT_ID,
                    basePlanId = "base_plan_monthly",
                    offerToken = "mock-token",
                    title = "Monthly",
                    subtitle = "Billed every month",
                    price = "",
                    rawBillingPeriod = "P1M"
                ),
                SubscriptionPlanOption(
                    productId = BillingConstants.PREMIUM_PRODUCT_ID,
                    basePlanId = "base_plan_yearly",
                    offerToken = "mock-token-2",
                    title = "Yearly",
                    subtitle = "Billed every year",
                    price = "",
                    rawBillingPeriod = "P1Y"
                )
            ),
            selectedPlanToken = "mock-token",
            onPlanSelected = {},
            onPurchase = {},
            onSelectFreePlan = {},
            snackbarHostState = remember { SnackbarHostState() }
        )
    }
}


