package com.example.ung_dung_chay_bo.presentation.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ung_dung_chay_bo.core.theme.LightBackground
import com.example.ung_dung_chay_bo.core.theme.LightBorder
import com.example.ung_dung_chay_bo.core.theme.PureWhite
import com.example.ung_dung_chay_bo.core.theme.SportOrange
import com.example.ung_dung_chay_bo.core.theme.SportOrangeSoft
import com.example.ung_dung_chay_bo.core.theme.TextMuted
import com.example.ung_dung_chay_bo.core.theme.TextPrimary
import com.example.ung_dung_chay_bo.core.theme.TextSecondary
import com.example.ung_dung_chay_bo.domain.model.ExperienceLevel
import com.example.ung_dung_chay_bo.domain.model.Gender
import com.example.ung_dung_chay_bo.domain.model.RunningGoal

@OptSpringPreview
@Composable
fun OnboardingScreen(
    onOnboardingComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.isCompleted) {
        if (uiState.isCompleted) {
            onOnboardingComplete()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.onErrorConsumed()
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = Color(0xFF1E293B),
                    contentColor = PureWhite,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        containerColor = PureWhite,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PureWhite)
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (uiState.currentStep > 1) {
                        IconButton(onClick = viewModel::onPreviousStep) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Quay lại",
                                tint = TextPrimary
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.size(48.dp))
                    }

                    Text(
                        text = "Bước ${uiState.currentStep} / 3",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextSecondary,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.size(48.dp))
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { uiState.currentStep / 3f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = SportOrange,
                    trackColor = LightBorder
                )
            }
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = PureWhite,
                shadowElevation = 8.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(20.dp)
                ) {
                    Button(
                        onClick = viewModel::onNextStep,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(27.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SportOrange,
                            contentColor = PureWhite
                        ),
                        enabled = !uiState.isLoading,
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = PureWhite,
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = if (uiState.currentStep == 3) "Hoàn tất & Khám phá" else "Tiếp tục",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        AnimatedContent(
            targetState = uiState.currentStep,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "OnboardingStepTransition",
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) { step ->
            when (step) {
                1 -> StepGoalSelection(
                    selectedGoal = uiState.selectedGoal,
                    onGoalSelected = viewModel::onGoalSelected
                )
                2 -> StepBodyMetrics(
                    gender = uiState.gender,
                    heightCm = uiState.heightCm,
                    weightKg = uiState.weightKg,
                    onGenderSelected = viewModel::onGenderSelected,
                    onHeightChange = viewModel::onHeightChange,
                    onWeightChange = viewModel::onWeightChange
                )
                3 -> StepExperienceAndPlan(
                    level = uiState.level,
                    weeklyFrequency = uiState.weeklyFrequency,
                    onLevelSelected = viewModel::onLevelSelected,
                    onFrequencyChange = viewModel::onFrequencyChange
                )
            }
        }
    }
}

@Composable
private fun StepGoalSelection(
    selectedGoal: RunningGoal,
    onGoalSelected: (RunningGoal) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Mục tiêu chính của bạn?",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = TextPrimary
        )
        Text(
            text = "Chúng tôi sẽ điều chỉnh kế hoạch chạy và thông số phù hợp nhất với bạn.",
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
            modifier = Modifier.padding(top = 6.dp, bottom = 24.dp)
        )

        RunningGoal.entries.forEach { goal ->
            val isSelected = goal == selectedGoal
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isSelected) SportOrangeSoft else PureWhite)
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) SportOrange else LightBorder,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable { onGoalSelected(goal) }
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = goal.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) SportOrange else TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = goal.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) SportOrange else LightBorder),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = PureWhite,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun StepBodyMetrics(
    gender: Gender,
    heightCm: Int,
    weightKg: Int,
    onGenderSelected: (Gender) -> Unit,
    onHeightChange: (Int) -> Unit,
    onWeightChange: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Thông số thể trạng",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = TextPrimary
        )
        Text(
            text = "Dữ liệu thể trạng giúp tính toán chính xác lượng Calo tiêu hao và nhịp bước.",
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
            modifier = Modifier.padding(top = 6.dp, bottom = 24.dp)
        )

        Text(
            text = "Giới tính",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Gender.entries.forEach { item ->
                val isSelected = item == gender
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) SportOrangeSoft else PureWhite)
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) SportOrange else LightBorder,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { onGenderSelected(item) }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) SportOrange else TextPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = "Chiều cao",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "$heightCm cm",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = SportOrange
            )
        }
        Slider(
            value = heightCm.toFloat(),
            onValueChange = { onHeightChange(it.toInt()) },
            valueRange = 120f..220f,
            colors = SliderDefaults.colors(
                thumbColor = SportOrange,
                activeTrackColor = SportOrange,
                inactiveTrackColor = LightBorder
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = "Cân nặng",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "$weightKg kg",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = SportOrange
            )
        }
        Slider(
            value = weightKg.toFloat(),
            onValueChange = { onWeightChange(it.toInt()) },
            valueRange = 35f..150f,
            colors = SliderDefaults.colors(
                thumbColor = SportOrange,
                activeTrackColor = SportOrange,
                inactiveTrackColor = LightBorder
            )
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun StepExperienceAndPlan(
    level: ExperienceLevel,
    weeklyFrequency: Int,
    onLevelSelected: (ExperienceLevel) -> Unit,
    onFrequencyChange: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Kinh nghiệm & Tần suất",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = TextPrimary
        )
        Text(
            text = "Hãy cho chúng tôi biết thói quen tập luyện hiện tại của bạn.",
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
            modifier = Modifier.padding(top = 6.dp, bottom = 24.dp)
        )

        Text(
            text = "Cấp độ chạy hiện tại",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))

        ExperienceLevel.entries.forEach { exp ->
            val isSelected = exp == level
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isSelected) SportOrangeSoft else PureWhite)
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) SportOrange else LightBorder,
                        shape = RoundedCornerShape(14.dp)
                    )
                    .clickable { onLevelSelected(exp) }
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = exp.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) SportOrange else TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = exp.desc,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = "Mục tiêu chạy mỗi tuần",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "$weeklyFrequency buổi / tuần",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = SportOrange
            )
        }
        Slider(
            value = weeklyFrequency.toFloat(),
            onValueChange = { onFrequencyChange(it.toInt()) },
            valueRange = 1f..7f,
            steps = 5,
            colors = SliderDefaults.colors(
                thumbColor = SportOrange,
                activeTrackColor = SportOrange,
                inactiveTrackColor = LightBorder
            )
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Retention(AnnotationRetention.BINARY)
annotation class OptSpringPreview
