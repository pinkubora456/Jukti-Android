import re

with open("app/src/main/java/com/example/ui/screens/BannerCarousel.kt", "r") as f:
    content = f.read()

# Update AutoShiftingBannerCarousel definition
content = content.replace(
"""fun AutoShiftingBannerCarousel(
    banners: List<com.example.data.local.BannerEntity>,
    language: com.example.ui.viewmodel.AppLanguage,
    onUpgradeClick: () -> Unit
)""",
"""fun AutoShiftingBannerCarousel(
    banners: List<com.example.data.local.BannerEntity>,
    plans: List<com.example.data.local.PlanEntity> = emptyList(),
    language: com.example.ui.viewmodel.AppLanguage,
    onUpgradeClick: () -> Unit
)""")

# Find pageCount logic
# val pageCount = if (banners.isNotEmpty()) banners.size + 1 else 1
new_pageCount_logic = """
    val activePlansCount = plans.count { it.isActive }
    val pageCount = banners.size + (if (activePlansCount > 0) activePlansCount else 1)
"""
content = re.sub(r'    val pageCount = if \(banners.isNotEmpty\(\)\) banners.size \+ 1 else 1', new_pageCount_logic, content)

# Update page logic
new_pager_logic = """
        HorizontalPager(
            state = pagerState, 
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        ) { page ->
            val activePlans = plans.filter { it.isActive }
            if (activePlans.isNotEmpty()) {
                if (page < activePlans.size) {
                    val plan = activePlans[page]
                    FeaturedPlanBanner(
                        plan = plan,
                        onBuyClick = onUpgradeClick
                    )
                } else {
                    val bannerIndex = page - activePlans.size
                    if (bannerIndex >= 0 && bannerIndex < banners.size) {
                        InfoBannerContent(banner = banners[bannerIndex], language = language, onUpgradeClick = onUpgradeClick)
                    }
                }
            } else {
                if (page == 0) {
                    // Fallback empty banner or dummy if no plans
                    FeaturedPlanBanner(
                        plan = com.example.data.local.PlanEntity(
                            planName = "Premium Access",
                            planPrice = "₹499",
                            discount = "0",
                            finalPrice = "499",
                            offerValidity = "",
                            features = "Full Access|All Mocks",
                            isActive = true
                        ),
                        onBuyClick = onUpgradeClick
                    )
                } else {
                    val bannerIndex = page - 1
                    if (bannerIndex >= 0 && bannerIndex < banners.size) {
                        InfoBannerContent(banner = banners[bannerIndex], language = language, onUpgradeClick = onUpgradeClick)
                    }
                }
            }
        }
"""
content = re.sub(r'    HorizontalPager\(.*?\}\n        \}\n    \}', new_pager_logic, content, flags=re.DOTALL)

with open("app/src/main/java/com/example/ui/screens/BannerCarousel.kt", "w") as f:
    f.write(content)
