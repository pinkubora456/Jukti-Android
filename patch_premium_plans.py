import re

with open("app/src/main/java/com/example/ui/screens/PremiumPlansScreen.kt", "r") as f:
    content = f.read()

# Add plan entity import
content = content.replace("import com.example.ui.viewmodel.JuktiViewModel", "import com.example.ui.viewmodel.JuktiViewModel\nimport com.example.data.local.PlanEntity")

# Rewrite PremiumPlansScreen
new_screen = """
@Composable
fun PremiumPlansScreen(viewModel: JuktiViewModel) {
    val language by viewModel.language.collectAsState()
    val plans by viewModel.plans.collectAsState()
    
    var selectedPlan by remember { mutableStateOf<PlanEntity?>(null) }
    var isPurchased by remember { mutableStateOf(false) }

    LaunchedEffect(plans) {
        if (plans.isNotEmpty() && selectedPlan == null) {
            selectedPlan = plans.firstOrNull { it.isActive }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (plans.isEmpty()) {
            Text("No plans available right now.", style = MaterialTheme.typography.titleMedium)
            return@Column
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.WorkspacePremium,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(60.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Jukti Premium Plans",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                selectedPlan?.let { plan ->
                    // Promotional Pricing
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (plan.planPrice.isNotBlank() && plan.planPrice != plan.finalPrice) {
                            Text(
                                text = "Fee ₹${plan.planPrice}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                                textDecoration = TextDecoration.LineThrough
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            if (plan.discount.isNotBlank() && plan.discount != "0") {
                                Surface(
                                    color = MaterialTheme.colorScheme.error,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "${plan.discount}% Off",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onError,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Pay Only ₹${plan.finalPrice}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))

        plans.filter { it.isActive }.forEach { plan ->
            val isSelected = (selectedPlan?.id == plan.id)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clickable { selectedPlan = plan },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(
                    2.dp,
                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(plan.planName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Text("₹${plan.finalPrice}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        selectedPlan?.let { plan ->
            val benefits = plan.features.split("|").filter { it.isNotBlank() }
            if (benefits.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Plan Benefits:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(10.dp))
                        benefits.forEach { benefit ->
                            Row(
                                modifier = Modifier.padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.success, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(benefit, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                isPurchased = true
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(if (isPurchased) "✅ Subscription Active!" else "Activate Plan Now")
        }
    }
}
"""

content = re.sub(r'@Composable\nfun PremiumPlansScreen.*?data class PlanOption.*?$', new_screen, content, flags=re.DOTALL)

with open("app/src/main/java/com/example/ui/screens/PremiumPlansScreen.kt", "w") as f:
    f.write(content)
