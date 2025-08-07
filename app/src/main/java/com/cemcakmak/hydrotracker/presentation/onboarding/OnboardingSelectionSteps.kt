package com.cemcakmak.hydrotracker.presentation.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import com.cemcakmak.hydrotracker.data.models.*

@Composable
fun GenderStep(
    selectedGender: Gender,
    onGenderSelected: (Gender) -> Unit,
    title: String,
    description: String
) {
    OnboardingStepLayout(
        title = title,
        description = description
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Gender.entries.forEach { gender ->
                SelectionCard(
                    title = gender.getDisplayName(),
                    description = gender.getPersonalizedGreeting(),
                    isSelected = selectedGender == gender,
                    onClick = { onGenderSelected(gender) },
                    icon = when (gender) {
                        Gender.MALE -> "👨"
                        Gender.FEMALE -> "👩"
                        Gender.OTHER -> "👤"
                    }
                )
            }
        }
    }
}

@Composable
fun AgeStep(
    selectedAgeGroup: AgeGroup,
    onAgeGroupSelected: (AgeGroup) -> Unit,
    title: String,
    description: String
) {
    OnboardingStepLayout(
        title = title,
        description = description
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AgeGroup.entries.forEach { ageGroup ->
                SelectionCard(
                    title = ageGroup.getDisplayName(),
                    description = ageGroup.getMotivationalMessage(),
                    isSelected = selectedAgeGroup == ageGroup,
                    onClick = { onAgeGroupSelected(ageGroup) },
                    icon = when (ageGroup) {
                        AgeGroup.YOUNG_ADULT_18_30 -> "🌟"
                        AgeGroup.ADULT_31_50 -> "💪"
                        AgeGroup.MIDDLE_AGED_51_60 -> "🌿"
                        AgeGroup.SENIOR_60_PLUS -> "🌸"
                    }
                )
            }
        }
    }
}

@Composable
fun ActivityStep(
    selectedActivityLevel: ActivityLevel,
    onActivityLevelSelected: (ActivityLevel) -> Unit,
    title: String,
    description: String
) {
    OnboardingStepLayout(
        title = title,
        description = description
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ActivityLevel.entries.forEach { activityLevel ->
                SelectionCard(
                    title = activityLevel.getDisplayName(),
                    description = activityLevel.getHydrationTip(),
                    isSelected = selectedActivityLevel == activityLevel,
                    onClick = { onActivityLevelSelected(activityLevel) },
                    icon = when (activityLevel) {
                        ActivityLevel.SEDENTARY -> "🪑"
                        ActivityLevel.LIGHT -> "🚶"
                        ActivityLevel.MODERATE -> "🏃"
                        ActivityLevel.ACTIVE -> "🏋️"
                        ActivityLevel.VERY_ACTIVE -> "🏃‍♂️"
                    }
                )
            }
        }
    }
}