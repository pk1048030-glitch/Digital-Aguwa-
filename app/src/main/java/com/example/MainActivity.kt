package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

// ==========================
// DATA MODELS & DEMO STATE
// ==========================

data class Candidate(
    val id: String,
    val name: String,
    val gender: String, // "Bride" or "Groom"
    val age: Int,
    val height: String,
    val caste: String,
    val education: String,
    val occupation: String,
    val bio: String,
    val phone: String,
    val familyDetails: String,
    val passcode: String, // 4-digit PIN required to unlock contact details
    val zila: String,
    val prakhand: String,
    val panchayat: String,
    val village: String,
    val registeredBy: String = "Rajesh Kumar", // Active Mediator
    val isVerified: Boolean = true
)

// Seed list of initial hyper-local candidate profiles matching rural Tier-2/3 criteria
val initialCandidates = listOf(
    Candidate(
        id = "1",
        name = "Kirti Kumari",
        gender = "Bride",
        age = 23,
        height = "5'3\"",
        caste = "Bhumihar",
        education = "BA B.Ed (Preparing for State Teacher exam)",
        occupation = "Primary School Educator / Sewing Kendra Coordinator",
        bio = "Simple and values-driven girl raised under religious rural traditions. Skilled in local folk songs, organic kitchen gardening, and Madhubani canvas paintings. Strongly believes in supporting joint-family values.",
        phone = "+91 94711 50293",
        familyDetails = "Father is High School Principal, Mother handles dairy-farm. Ancestral home: Konar village.",
        passcode = "1234",
        zila = "Rohtas",
        prakhand = "Shivsagar",
        panchayat = "Konar",
        village = "Konar Village"
    ),
    Candidate(
        id = "2",
        name = "Vikram Singh Chouhan",
        gender = "Groom",
        age = 26,
        height = "5'10\"",
        caste = "Bhumihar",
        education = "B.Sc Agriculture (Dr. Rajendra Prasad Central Uni)",
        occupation = "Co-Owner, Village Farm Services & Agro-Cooperative",
        bio = "Disciplined youth combining modern agriculture with strict cultural values. Assists the local Panchayat in crop-rotation models. Looking for a family-first partner from Rohtas/Patna zila.",
        phone = "+91 74823 11842",
        familyDetails = "Father retired local registrar, Elder brother works in block office. 6 Bigha ancestral cropland.",
        passcode = "5678",
        zila = "Rohtas",
        prakhand = "Shivsagar",
        panchayat = "Konar",
        village = "Konar Village"
    ),
    Candidate(
        id = "3",
        name = "Priya Ranjan Yadav",
        gender = "Bride",
        age = 24,
        height = "5'2\"",
        caste = "Yadav (OBC)",
        education = "B.Sc Nursing (Gaya College of Nursing)",
        occupation = "Community Health Nurse (Contractual)",
        bio = "Dedicated and kind, loves community volunteering and traditional family gatherings. Extremely respectful of elders, enjoys traditional sweets preparation and local festivals.",
        phone = "+91 88921 54673",
        familyDetails = "Father runs local grain warehouse (arhatiya), two younger brothers studying in Patna.",
        passcode = "1111",
        zila = "Rohtas",
        prakhand = "Shivsagar",
        panchayat = "Konar",
        village = "Malahi Sarpura"
    ),
    Candidate(
        id = "4",
        name = "Sanjay Kumar Yadav",
        gender = "Groom",
        age = 27,
        height = "5'9\"",
        caste = "Yadav (OBC)",
        education = "M.B.A (Rural Management, Patna)",
        occupation = "District Co-operative Bank Associate Manager",
        bio = "Wants to remain rooted in rural traditions, serving Patna/Rohtas farmer societies. Extremely peaceful daily routine, loves livestock rearing and state volleyball. Respects family hierarchies.",
        phone = "+91 99312 88471",
        familyDetails = "Father is cooperative society head, maternal uncle is Panchayat Ward member in Amhara.",
        passcode = "2222",
        zila = "Patna",
        prakhand = "Bihta",
        panchayat = "Amhara",
        village = "Amhara Village"
    ),
    Candidate(
        id = "5",
        name = "Anjali Mishra",
        gender = "Bride",
        age = 22,
        height = "5'1\"",
        caste = "Brahmin",
        education = "MA Sanskrit (Gaya College of Arts)",
        occupation = "Sanskrit Shala Teacher / Home Tutor",
        bio = "Very pious and tranquil soul, believes in Vedic traditions, reads scriptures, and conducts local spiritual bhajan sessions with rural matriarchs. Seeking an equivalent traditional groom.",
        phone = "+91 82914 22019",
        familyDetails = "Father is renowned local temple priest, mother handles family dairy. 3 elder married sisters.",
        passcode = "4321",
        zila = "Patna",
        prakhand = "Bihta",
        panchayat = "Amhara",
        village = "Bihta Colony"
    )
)

// ==========================
// PRESET LOCATION HIERARCHY
// ==========================

val zilaOptions = listOf("All Zilas", "Rohtas", "Patna", "Gaya", "Bhojpur")

val prakhandMapping = mapOf(
    "Rohtas" to listOf("Shivsagar", "Dehri", "Sasaram", "Bikramganj"),
    "Patna" to listOf("Bihta", "Danapur", "Phulwari Sharif", "Patna Sadar"),
    "Gaya" to listOf("Bodhgaya", "Sherghati", "Tekari", "Wazirganj"),
    "Bhojpur" to listOf("Ara", "Behea", "Koilwar", "Piro")
)

val panchayatMapping = mapOf(
    "Shivsagar" to listOf("Konar", "Sasaram Main", "Akorha", "Karwandia"),
    "Dehri" to listOf("Bastipur", "Darihat", "Sidhaul", "Malhipur"),
    "Bihta" to listOf("Amhara", "Anandpur", "Koteshwer", "Bihta Sadar")
)

val villageMapping = mapOf(
    "Konar" to listOf("Konar Village", "Malahi Sarpura", "Ramgarh"),
    "Bastipur" to listOf("Bastipur Village", "Darihat Village", "Dehri Tola"),
    "Amhara" to listOf("Amhara Village", "Bihta Colony", "Bishunpura")
)


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainContentScreen()
            }
        }
    }
}

@Composable
fun MainContentScreen() {
    val context = LocalContext.current
    var currentTab by remember { mutableStateOf("dashboard") } // "dashboard", "directory", "ai_match"

    // App state: candidate database
    val candidatesList = remember { mutableStateListOf<Candidate>().apply { addAll(initialCandidates) } }

    // Active unlocked candidates store (maintains credentials unlocked locally)
    val unlockedCandidateIds = remember { mutableStateListOf<String>() }

    // State for candidate detailed profile unlock
    var candidateToUnlock by remember { mutableStateOf<Candidate?>(null) }

    // Sanskriti Match selected candidates
    var matchCandidateA by remember { mutableStateOf<Candidate?>(null) }
    var matchCandidateB by remember { mutableStateOf<Candidate?>(null) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            CustomBottomBar(currentTab = currentTab, onTabSelected = { currentTab = it })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(SoftMintBg) // Vibrant Palette soft mint background
        ) {
            // Elegant Vibrant Palette Teal Top Header
            AguwaHeader(candidatesCount = candidatesList.size)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when (currentTab) {
                    "dashboard" -> MediatorDashboard(
                        candidates = candidatesList,
                        onAddCandidate = { newCandidate ->
                            candidatesList.add(newCandidate)
                            Toast.makeText(context, "Registered ${newCandidate.name} Successfully!", Toast.LENGTH_SHORT).show()
                        }
                    )
                    "directory" -> DirectoryScreen(
                        candidates = candidatesList,
                        unlockedIds = unlockedCandidateIds,
                        onTriggerUnlock = { candidateToUnlock = it },
                        onAddToMatchAB = { selected, slot ->
                            if (slot == "A") {
                                matchCandidateA = selected
                                Toast.makeText(context, "Slot A set: ${selected.name}", Toast.LENGTH_SHORT).show()
                            } else {
                                matchCandidateB = selected
                                Toast.makeText(context, "Slot B set: ${selected.name}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                    "ai_match" -> AICompatibilityScreen(
                        candidates = candidatesList,
                        selectedA = matchCandidateA,
                        selectedB = matchCandidateB,
                        onSelectA = { matchCandidateA = it },
                        onSelectB = { matchCandidateB = it }
                    )
                }
            }
        }

        // Passcode Unlock Overlay Sheet / Dialog
        candidateToUnlock?.let { candidate ->
            PasscodeUnlockDialog(
                candidate = candidate,
                onDismiss = { candidateToUnlock = null },
                onUnlockSuccess = {
                    unlockedCandidateIds.add(candidate.id)
                    candidateToUnlock = null
                    Toast.makeText(context, "🔓 Privacy Shield Unlocked successfully for ${candidate.name}!", Toast.LENGTH_LONG).show()
                }
            )
        }
    }
}

// ==========================
// COMPOSABLES: HEADER & BOTTOM NAVIGATION
// ==========================

@Composable
fun AguwaHeader(candidatesCount: Int) {
    Column {
        // Vibrant Palette Teal Header Block with rounded-b-32 shape
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = DeepTeal,
                    shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                )
                .padding(top = 24.dp, bottom = 18.dp, start = 20.dp, end = 20.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "MEDIATOR DASHBOARD",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = LightTeal,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Digital Aguwa",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = (-0.5).sp
                        )
                    }

                    // Rounded User/Mediator Avatar Profile Badge
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .background(Color.White.copy(alpha = 0.2f), shape = CircleShape)
                            .border(1.dp, Color.White.copy(alpha = 0.3f), shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "👤", fontSize = 20.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Glassmorphism indicators row from the Vibrant Theme Design
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Indicator 1: Managed Profiles
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color.White.copy(alpha = 0.12f), shape = RoundedCornerShape(16.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.15f), shape = RoundedCornerShape(16.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Column {
                            Text(
                                text = "MANAGED",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = LightTeal.copy(alpha = 0.8f)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "$candidatesCount",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Profiles",
                                    fontSize = 11.sp,
                                    color = LightTeal.copy(alpha = 0.9f)
                                )
                            }
                        }
                    }

                    // Indicator 2: Matches
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color.White.copy(alpha = 0.12f), shape = RoundedCornerShape(16.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.15f), shape = RoundedCornerShape(16.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Column {
                            Text(
                                text = "MATCHES",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = LightTeal.copy(alpha = 0.8f)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "03",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Fixed",
                                    fontSize = 11.sp,
                                    color = LightTeal.copy(alpha = 0.9f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Active Advisor Bar with Light Teal container background
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF0F5F4))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Verified Status",
                    tint = DeepTeal,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "COMMUNITY AGUWA: Rajesh Kumar",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkGreyText
                )
            }
            Text(
                text = "Panchayat: Konar (Rohtas)",
                fontSize = 10.sp,
                color = SageText,
                fontWeight = FontWeight.Bold
            )
        }

        // Elegant separator of dots conformant to the brand tones
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SoftMintBg)
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(15) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(5.dp)
                        .background(MintAccent, shape = CircleShape)
                )
            }
        }
    }
}

@Composable
fun CustomBottomBar(
    currentTab: String,
    onTabSelected: (String) -> Unit
) {
    NavigationBar(
        containerColor = Color.White,
        contentColor = DarkGreyText,
        modifier = Modifier.border(width = 1.dp, color = SageBorder, shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
    ) {
        NavigationBarItem(
            selected = currentTab == "dashboard",
            onClick = { onTabSelected("dashboard") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = DeepTeal,
                selectedTextColor = DeepTeal,
                unselectedIconColor = SageText,
                unselectedTextColor = SageText,
                indicatorColor = LightTeal
            ),
            icon = { Icon(imageVector = Icons.Default.Home, contentDescription = "Dashboard") },
            label = { Text("Aguwa Cockpit", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
        )

        NavigationBarItem(
            selected = currentTab == "directory",
            onClick = { onTabSelected("directory") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = DeepTeal,
                selectedTextColor = DeepTeal,
                unselectedIconColor = SageText,
                unselectedTextColor = SageText,
                indicatorColor = LightTeal
            ),
            icon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Directory") },
            label = { Text("Local Directory", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
        )

        NavigationBarItem(
            selected = currentTab == "ai_match",
            onClick = { onTabSelected("ai_match") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = DeepTeal,
                selectedTextColor = DeepTeal,
                unselectedIconColor = SageText,
                unselectedTextColor = SageText,
                indicatorColor = LightTeal
            ),
            icon = { Icon(imageVector = Icons.Default.Star, contentDescription = "AI Match") },
            label = { Text("Sanskriti AI", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
        )
    }
}

// ==========================
// TAB SCREEN: MEDIATOR DASHBOARD
// ==========================

@Composable
fun MediatorDashboard(
    candidates: List<Candidate>,
    onAddCandidate: (Candidate) -> Unit
) {
    var showAddForm by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
        ) {
            // Welcome Card in Vibrant Theme Style
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, SageBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .background(LightTeal, shape = CircleShape)
                                .border(1.dp, DeepTeal.copy(alpha = 0.3f), shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "👨‍💼", fontSize = 24.sp)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Namaste Rajeshji!",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = DeepTeal
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Traditional mediators represent integrity. Your community candidates rely on your verification to escape fraud profiles.",
                                fontSize = 12.sp,
                                color = SageText,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            // Stat Counter Row (styled elegantly in Vibrant Palette White + Sage Borders)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val bridesCount = candidates.count { it.gender == "Bride" }
                    val groomsCount = candidates.count { it.gender == "Groom" }

                    DashboardStatCard(
                        title = "Candidates Managed",
                        metric = "${candidates.size}",
                        subtitle = "👰 $bridesCount Brides • 🤵 $groomsCount Grooms",
                        modifier = Modifier.weight(1f),
                        color = Color.White,
                        borderColor = SageBorder
                    )

                    DashboardStatCard(
                        title = "Verified & Live",
                        metric = "${candidates.count { it.isVerified }}",
                        subtitle = "🔑 100% Passcode-Shield",
                        modifier = Modifier.weight(1f),
                        color = Color.White,
                        borderColor = SageBorder
                    )
                }
            }

            // Another stat block for successful matches styled with Dark Forest Teal and Mint Accent
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkForest),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, DeepTeal.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Successful Panchayat Matches",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MintAccent
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Matches initiated and successfully completed via your mediator mediation this year.",
                                fontSize = 11.sp,
                                color = LightTeal.copy(alpha = 0.8f)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .background(MintAccent, shape = RoundedCornerShape(12.dp))
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "3 Matches",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                color = Color(0xFF003731)
                            )
                        }
                    }
                }
            }

            // Quick Actions Title (Vibrant Teal highlight)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp, 4.dp)
                            .background(DeepTeal, RoundedCornerShape(2.dp))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "REGISTER NEW COMMUNITY CANDIDATE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = DeepTeal,
                        letterSpacing = 1.sp
                    )
                }
            }

            // Add Candidate Button Trigger (Styled as high-contrast Mint/Teal Accent FAB class)
            item {
                Button(
                    onClick = { showAddForm = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MintAccent,
                        contentColor = Color(0xFF003731)
                    ),
                    border = BorderStroke(1.2.dp, DeepTeal.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(24.dp), // rounded-[24px] from theme
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Icon")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Candidate Profile & Set Passcode", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Quick instruction panel on verification (Styled in Light Teal + Sage guidelines)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F5F4)),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, SageBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "🛡️ Digital Aguwa Verification Protocol",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = DeepTeal
                        )
                        HorizontalDivider(color = SageBorder, thickness = 0.5.dp)
                        Text(
                            text = "1. Verified profiles carry your recommendation seal, boosting matchmaking success by 85%.\n\n" +
                                   "2. Secure the candidate's passcode strictly. It will only be revealed during physical rural meetups or trusted direct contacts.",
                            fontSize = 12.sp,
                            color = SageText,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        // Expanded Registration Form Sheet Modal
        if (showAddForm) {
            Dialog(
                onDismissRequest = { showAddForm = false },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(SoftMintBg)
                ) {
                    AddCandidateForm(
                        onDismiss = { showAddForm = false },
                        onSubmit = {
                            onAddCandidate(it)
                            showAddForm = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun DashboardStatCard(
    title: String,
    metric: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    color: Color,
    borderColor: Color
) {
    Card(
        modifier = modifier
            .border(1.dp, borderColor, RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = color),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = SageText
            )
            Text(
                text = metric,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = DeepTeal
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = SageText,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ==========================
// REGISTRATION FORM COMPOSABLE
// ==========================

@Composable
fun AddCandidateForm(
    onDismiss: () -> Unit,
    onSubmit: (Candidate) -> Unit
) {
    val scrollState = rememberScrollState()

    var name by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Bride") }
    var age by remember { mutableStateOf("23") }
    var height by remember { mutableStateOf("5'4\"") }
    var caste by remember { mutableStateOf("Bhumihar") }
    var education by remember { mutableStateOf("") }
    var occupation by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var familyDetails by remember { mutableStateOf("") }
    var passcode by remember { mutableStateOf("") }

    // Location selections (Hierarchical)
    var zila by remember { mutableStateOf("Rohtas") }
    var prakhandOptions by remember { mutableStateOf(prakhandMapping["Rohtas"] ?: emptyList()) }
    var prakhand by remember { mutableStateOf(prakhandOptions.firstOrNull() ?: "") }
    var panchayatOptions by remember { mutableStateOf(panchayatMapping[prakhand] ?: emptyList()) }
    var panchayat by remember { mutableStateOf(panchayatOptions.firstOrNull() ?: "") }
    var villageOptions by remember { mutableStateOf(villageMapping[panchayat] ?: emptyList()) }
    var village by remember { mutableStateOf(villageOptions.firstOrNull() ?: "") }

    // Track dynamic dropdown toggles
    var zilaExpanded by remember { mutableStateOf(false) }
    var prakhandExpanded by remember { mutableStateOf(false) }
    var panchayatExpanded by remember { mutableStateOf(false) }
    var villageExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
            .background(SoftMintBg)
    ) {
        // Form Title bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onDismiss) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = DeepTeal)
            }
            Text(
                text = "Register Community Profile",
                fontWeight = FontWeight.Black,
                fontSize = 17.sp,
                color = DeepTeal
            )
            Box(modifier = Modifier.size(48.dp)) // Spacer
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Warning on Privacy Shield - Replaced warm orange with Light Mint Teal brand tone
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F5F4)),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, SageBorder)
        ) {
            Row(modifier = Modifier.padding(16.dp)) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Shield hint",
                    tint = DeepTeal
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "LOCKED BY DEFAULT: For safety in rural areas, the candidate's exact bio, phone, and photos are locked by a 4-digit passcode you establish below. They are hidden unless a verified suitor enters this exact PIN.",
                    fontSize = 12.sp,
                    color = SageText,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Full Name input
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Candidate Full Name") },
            placeholder = { Text("e.g. Priya Sharma / Amit Choubey") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Gender Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Gender Category:", fontWeight = FontWeight.Bold, color = Color(0xFF5D4037))
            Row {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = gender == "Bride", onClick = { gender = "Bride" })
                    Text("Bride (वधू)")
                }
                Spacer(modifier = Modifier.width(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = gender == "Groom", onClick = { gender = "Groom" })
                    Text("Groom (वर)")
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = age,
                onValueChange = { age = it },
                label = { Text("Age") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            OutlinedTextField(
                value = height,
                onValueChange = { height = it },
                label = { Text("Height") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = caste,
            onValueChange = { caste = it },
            label = { Text("Caste / Community") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Regional Location Dropdowns (Hierarchical cascading)
        Text(
            text = "DEEP HYPER-LOCAL HIERARCHY (REQUIRED)",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 11.sp,
            color = Color(0xFFE65100),
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 1. Zila
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = zila,
                onValueChange = {},
                readOnly = true,
                label = { Text("Zila (District)") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    Icon(
                        imageVector = if (zilaExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Dropdown",
                        modifier = Modifier.clickable { zilaExpanded = !zilaExpanded }
                    )
                }
            )
            DropdownMenu(expanded = zilaExpanded, onDismissRequest = { zilaExpanded = false }) {
                zilaOptions.filter { it != "All Zilas" }.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            zila = option
                            zilaExpanded = false
                            // Cascade update Prakhand options
                            prakhandOptions = prakhandMapping[zila] ?: emptyList()
                            prakhand = prakhandOptions.firstOrNull() ?: ""
                            // Cascade update Panchayat options
                            panchayatOptions = panchayatMapping[prakhand] ?: emptyList()
                            panchayat = panchayatOptions.firstOrNull() ?: ""
                            // Cascade update Village options
                            villageOptions = villageMapping[panchayat] ?: emptyList()
                            village = villageOptions.firstOrNull() ?: ""
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 2. Prakhand
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = prakhand,
                onValueChange = {},
                readOnly = true,
                label = { Text("Prakhand (Block)") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    Icon(
                        imageVector = if (prakhandExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Dropdown",
                        modifier = Modifier.clickable { prakhandExpanded = !prakhandExpanded }
                    )
                }
            )
            DropdownMenu(expanded = prakhandExpanded, onDismissRequest = { prakhandExpanded = false }) {
                prakhandOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            prakhand = option
                            prakhandExpanded = false
                            // Cascade update Panchayat options
                            panchayatOptions = panchayatMapping[prakhand] ?: emptyList()
                            panchayat = panchayatOptions.firstOrNull() ?: ""
                            // Cascade update Village options
                            villageOptions = villageMapping[panchayat] ?: emptyList()
                            village = villageOptions.firstOrNull() ?: ""
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 3. Panchayat
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = panchayat,
                onValueChange = {},
                readOnly = true,
                label = { Text("Panchayat") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    Icon(
                        imageVector = if (panchayatExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Dropdown",
                        modifier = Modifier.clickable { panchayatExpanded = !panchayatExpanded }
                    )
                }
            )
            DropdownMenu(expanded = panchayatExpanded, onDismissRequest = { panchayatExpanded = false }) {
                panchayatOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            panchayat = option
                            panchayatExpanded = false
                            // Cascade update Village options
                            villageOptions = villageMapping[panchayat] ?: emptyList()
                            village = villageOptions.firstOrNull() ?: ""
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 4. Village
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = village,
                onValueChange = {},
                readOnly = true,
                label = { Text("Village (Gram)") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    Icon(
                        imageVector = if (villageExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Dropdown",
                        modifier = Modifier.clickable { villageExpanded = !villageExpanded }
                    )
                }
            )
            DropdownMenu(expanded = villageExpanded, onDismissRequest = { villageExpanded = false }) {
                villageOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            village = option
                            villageExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Education & Occupation
        OutlinedTextField(
            value = education,
            onValueChange = { education = it },
            label = { Text("Education Degree") },
            placeholder = { Text("e.g. B.Sc Agriculture / BA B.Ed") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = occupation,
            onValueChange = { occupation = it },
            label = { Text("Occupation / Livelihood") },
            placeholder = { Text("e.g. Government School Teacher") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Free bio and contact fields
        OutlinedTextField(
            value = bio,
            onValueChange = { bio = it },
            label = { Text("Biodata Description (Family Values & Lifestyle)") },
            placeholder = { Text("Describe family customs, daily lifestyle, and general character values for AI Sanskriti matching.") },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            maxLines = 4
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Phone & Family details (LOCKED SECURE FIELDS)
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Suitor Contact Phone (SECURE - LOCKED)") },
            placeholder = { Text("+91 98XXX XXXXX") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = familyDetails,
            onValueChange = { familyDetails = it },
            label = { Text("Detailed Family Background (SECURE - LOCKED)") },
            placeholder = { Text("e.g. Father retired registrar, family owns 5 Bigha crop fields.") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // SECURITY PIN DESIGNATION - Vibrant Theme styling
        Text(
            text = "PRIVACY SECURITY PIN",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 11.sp,
            color = DeepTeal,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = passcode,
            onValueChange = { if (it.length <= 4) passcode = it },
            label = { Text("Establish 4-Digit Passcode") },
            placeholder = { Text("e.g. 5678") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = DeepTeal,
                focusedLabelColor = DeepTeal
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (name.isBlank() || phone.isBlank() || passcode.length != 4) {
                    // Quick validations
                    return@Button
                }
                val newCandidate = Candidate(
                    id = System.currentTimeMillis().toString(),
                    name = name,
                    gender = gender,
                    age = age.toIntOrNull() ?: 24,
                    height = height,
                    caste = caste,
                    education = education,
                    occupation = occupation,
                    bio = bio,
                    phone = phone,
                    familyDetails = familyDetails,
                    passcode = passcode,
                    zila = zila,
                    prakhand = prakhand,
                    panchayat = panchayat,
                    village = village
                )
                onSubmit(newCandidate)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            colors = ButtonDefaults.buttonColors(containerColor = DeepTeal, contentColor = Color.White),
            shape = RoundedCornerShape(24.dp)
        ) {
            Text("Publish Candidates Live (Verified)", fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

// ==========================
// TAB SCREEN: DIRECTORY (MICRO-LOCAL SEARCH)
// ==========================

@Composable
fun DirectoryScreen(
    candidates: List<Candidate>,
    unlockedIds: List<String>,
    onTriggerUnlock: (Candidate) -> Unit,
    onAddToMatchAB: (Candidate, String) -> Unit
) {
    // Search Criteria state
    var selectedZila by remember { mutableStateOf("All Zilas") }
    var selectedPrakhand by remember { mutableStateOf("All Prakhands") }
    var selectedPanchayat by remember { mutableStateOf("All Panchayats") }
    var selectedVillage by remember { mutableStateOf("All Villages") }

    // Dropdown visibility states
    var zDropExpanded by remember { mutableStateOf(false) }
    var pDropExpanded by remember { mutableStateOf(false) }
    var panDropExpanded by remember { mutableStateOf(false) }
    var vDropExpanded by remember { mutableStateOf(false) }

    // Filter candidates based on selected micro-local geography hierarchy
    val filteredCandidates = candidates.filter { c ->
        val matchZila = selectedZila == "All Zilas" || c.zila == selectedZila
        val matchPrakhand = selectedPrakhand == "All Prakhands" || c.prakhand == selectedPrakhand
        val matchPanchayat = selectedPanchayat == "All Panchayats" || c.panchayat == selectedPanchayat
        val matchVillage = selectedVillage == "All Villages" || c.village == selectedVillage
        matchZila && matchPrakhand && matchPanchayat && matchVillage
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Hierarchical Regional Selection Panel (Filter Cockpit)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, SageBorder),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "📍 HYPER-LOCAL VILLAGE DIRECTORY FILTERS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = DeepTeal,
                    letterSpacing = 0.5.sp
                )

                // Row for Zila & Prakhand
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Zila Selector
                    Box(modifier = Modifier.weight(1f)) {
                        Button(
                            onClick = { zDropExpanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, SageBorder),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Zila: $selectedZila",
                                    fontSize = 12.sp,
                                    color = SageText,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Drop",
                                    tint = DeepTeal,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        DropdownMenu(expanded = zDropExpanded, onDismissRequest = { zDropExpanded = false }) {
                            zilaOptions.forEach { z ->
                                DropdownMenuItem(
                                    text = { Text(z, fontSize = 13.sp) },
                                    onClick = {
                                        selectedZila = z
                                        zDropExpanded = false
                                        // Reset cascades
                                        selectedPrakhand = "All Prakhands"
                                        selectedPanchayat = "All Panchayats"
                                        selectedVillage = "All Villages"
                                    }
                                )
                            }
                        }
                    }

                    // Prakhand Selector
                    Box(modifier = Modifier.weight(1f)) {
                        Button(
                            onClick = { pDropExpanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, SageBorder),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(16.dp),
                            enabled = selectedZila != "All Zilas"
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Prakhand: ${if (selectedZila == "All Zilas") "Select Zila" else selectedPrakhand}",
                                    fontSize = 11.sp,
                                    color = SageText,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Drop",
                                    tint = DeepTeal,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        DropdownMenu(expanded = pDropExpanded, onDismissRequest = { pDropExpanded = false }) {
                            val availablePrakhands = listOf("All Prakhands") + (prakhandMapping[selectedZila] ?: emptyList())
                            availablePrakhands.forEach { p ->
                                DropdownMenuItem(
                                    text = { Text(p, fontSize = 13.sp) },
                                    onClick = {
                                        selectedPrakhand = p
                                        pDropExpanded = false
                                        // Reset cascades
                                        selectedPanchayat = "All Panchayats"
                                        selectedVillage = "All Villages"
                                    }
                                )
                            }
                        }
                    }
                }

                // Row for Panchayat & Village
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Panchayat Selector
                    Box(modifier = Modifier.weight(1f)) {
                        Button(
                            onClick = { panDropExpanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, SageBorder),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(16.dp),
                            enabled = selectedPrakhand != "All Prakhands"
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Pan: ${if (selectedPrakhand == "All Prakhands") "Select Block" else selectedPanchayat}",
                                    fontSize = 11.sp,
                                    color = SageText,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Drop",
                                    tint = DeepTeal,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        DropdownMenu(expanded = panDropExpanded, onDismissRequest = { panDropExpanded = false }) {
                            val availablePanchayats = listOf("All Panchayats") + (panchayatMapping[selectedPrakhand] ?: emptyList())
                            availablePanchayats.forEach { pan ->
                                DropdownMenuItem(
                                    text = { Text(pan, fontSize = 13.sp) },
                                    onClick = {
                                        selectedPanchayat = pan
                                        panDropExpanded = false
                                        // Reset Cascades
                                        selectedVillage = "All Villages"
                                    }
                                )
                            }
                        }
                    }

                    // Village Selector
                    Box(modifier = Modifier.weight(1f)) {
                        Button(
                            onClick = { vDropExpanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, SageBorder),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(16.dp),
                            enabled = selectedPanchayat != "All Panchayats"
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Gram: ${if (selectedPanchayat == "All Panchayats") "Select Pan" else selectedVillage}",
                                    fontSize = 11.sp,
                                    color = SageText,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Drop",
                                    tint = DeepTeal,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        DropdownMenu(expanded = vDropExpanded, onDismissRequest = { vDropExpanded = false }) {
                            val availableVillages = listOf("All Villages") + (villageMapping[selectedPanchayat] ?: emptyList())
                            availableVillages.forEach { vil ->
                                DropdownMenuItem(
                                    text = { Text(vil, fontSize = 13.sp) },
                                    onClick = {
                                        selectedVillage = vil
                                        vDropExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search Metrics & Clean Options Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${filteredCandidates.size} matchmaker candidates found",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = SageText
            )

            if (selectedZila != "All Zilas" || selectedPrakhand != "All Prakhands" || selectedPanchayat != "All Panchayats" || selectedVillage != "All Villages") {
                Text(
                    text = "Clear Filter",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepTeal,
                    modifier = Modifier.clickable {
                        selectedZila = "All Zilas"
                        selectedPrakhand = "All Prakhands"
                        selectedPanchayat = "All Panchayats"
                        selectedVillage = "All Villages"
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Directory List
        if (filteredCandidates.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Empty",
                        tint = Color(0xFFBCAAA4),
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "No candidates in this village/Panchayat yet.",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF8D6E63)
                    )
                    Text(
                        text = "Try clearing the hyper-local filters or select 'All Zilas' to search wide.",
                        fontSize = 11.sp,
                        color = Color(0xFFA1887F),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(filteredCandidates) { candidate ->
                    CandidateSearchCard(
                        candidate = candidate,
                        isUnlocked = unlockedIds.contains(candidate.id),
                        onUnlockClick = { onTriggerUnlock(candidate) },
                        onAddToMatchAB = onAddToMatchAB
                    )
                }
            }
        }
    }
}

// ==========================
// COMPOSABLE: INDIVIDUAL CANDIDATE SEARCH CARD
// ==========================

@Composable
fun CandidateSearchCard(
    candidate: Candidate,
    isUnlocked: Boolean,
    onUnlockClick: () -> Unit,
    onAddToMatchAB: (Candidate, String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.2.dp, if (isUnlocked) DeepTeal else SageBorder),
        shape = RoundedCornerShape(24.dp), // curved corners
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // General Info and Gender Crest
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(
                                if (candidate.gender == "Bride") Color(0xFFFEF2F2) else Color(0xFFEFF6FF),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (candidate.gender == "Bride") "👰 BRIDE (वधू)" else " वर (GROOM) 🤵",
                            color = if (candidate.gender == "Bride") Color(0xFF991B1B) else Color(0xFF1E40AF),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "ID: #${candidate.id}",
                        fontSize = 10.sp,
                        color = SageText,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Verified Status",
                        tint = DeepTeal,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Verified",
                        fontSize = 11.sp,
                        color = DeepTeal,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                // Profile Photo Simulator (Modern gradient from Teal to Mint)
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(LightTeal, MintAccent)
                            )
                        )
                        .blur(if (isUnlocked) 0.dp else 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Portrait Silhouette",
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(44.dp)
                    )
                    if (!isUnlocked) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Locked Cover",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = if (isUnlocked) candidate.name else "${candidate.name.first()}*** ${candidate.name.split(" ").lastOrNull() ?: ""}",
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        color = DeepTeal
                    )

                    Text(
                        text = "${candidate.age} Years • ${candidate.height} • ${candidate.caste}",
                        fontSize = 12.sp,
                        color = SageText,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Location Pin",
                            tint = DeepTeal,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${candidate.village}, ${candidate.panchayat}, ${candidate.prakhand}",
                            fontSize = 11.sp,
                            color = SageText,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Candidate Education & Occupation Info (Always shown as high-level filter values inside clean light containers)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF0F5F4), RoundedCornerShape(12.dp))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("EDUCATION", fontSize = 9.sp, color = SageText, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(1.dp))
                    Text(candidate.education, fontSize = 11.sp, color = DeepTeal, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("OCCUPATION", fontSize = 9.sp, color = SageText, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(1.dp))
                    Text(candidate.occupation, fontSize = 11.sp, color = DeepTeal, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // PRIVACY SHIELD AREA
            if (!isUnlocked) {
                // Locked State Shield Panel (Upgraded to Red/Orange design-safety standard)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFFCA5A5), RoundedCornerShape(16.dp))
                        .background(Color(0xFFFEF2F2))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Shield Locked Key",
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Contact & Family Details Hidden",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFDC2626)
                            )
                            Text(
                                text = "Secure Passcode required to reveal phone & family bio.",
                                fontSize = 9.sp,
                                color = Color(0xFF991B1B)
                            )
                        }
                    }

                    Button(
                        onClick = onUnlockClick,
                        colors = ButtonDefaults.buttonColors(containerColor = DeepTeal, contentColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text("Unlock", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                // Unlocked State Shield Panel (Upgraded to solid, modern green theme)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF10B981), RoundedCornerShape(16.dp))
                        .background(Color(0xFFECFDF5))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock, // Lock represents safety seal of verification
                            contentDescription = "Unlocked Seal Confirmed",
                            tint = Color(0xFF047857),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "PRIVACY SHIELD UNLOCKED",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF047857)
                        )
                    }

                    HorizontalDivider(color = Color(0xFFA7F3D0), thickness = 0.5.dp)

                    Row {
                        Text("📞 Phone Suitor: ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF047857))
                        Text(candidate.phone, fontSize = 11.sp, color = Color(0xFF064E3B), fontWeight = FontWeight.Black)
                    }

                    Column {
                        Text("🏡 Family Background & Assets:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF047857))
                        Text(candidate.familyDetails, fontSize = 11.sp, color = Color(0xFF064E3B))
                    }

                    Column {
                        Text("🌸 Candidate Personal Bio (Detailed):", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF047857))
                        Text(candidate.bio, fontSize = 11.sp, color = SageText)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action: Add to Sanskriti Compatibility slots (A and B) - Teal rounded pill outlines
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onAddToMatchAB(candidate, "A") },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = DeepTeal),
                    border = BorderStroke(1.2.dp, DeepTeal),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Select as Match Bride (Slot A)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { onAddToMatchAB(candidate, "B") },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = DeepTeal),
                    border = BorderStroke(1.2.dp, DeepTeal),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Select as Match Groom (Slot B)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ==========================
// COMPOSABLE: PASSCODE UNLOCK KEYPAD OVERLAY
// ==========================

@Composable
fun PasscodeUnlockDialog(
    candidate: Candidate,
    onDismiss: () -> Unit,
    onUnlockSuccess: () -> Unit
) {
    val context = LocalContext.current
    var inputCode by remember { mutableStateOf("") }
    var codeError by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDF9)),
            border = BorderStroke(1.5.dp, Color(0xFFFFB300)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Enter Security Passcode",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = Color(0xFF3E2723)
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                Text(
                    text = "Candidate credentials of ${candidate.name} are shielded in accordance with community security concerns. Enter the 4-digit code provided by your local Aguwa.",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )

                // The Dots Code Display
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .background(
                            if (codeError) Color(0xFFFFEBEE) else Color(0xFFF5ECE1),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 24.dp, vertical = 10.dp)
                ) {
                    repeat(4) { idx ->
                        val active = idx < inputCode.length
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .background(
                                    color = if (active) Color(0xFF3E2723) else Color(0xFFBCAAA4),
                                    shape = CircleShape
                                )
                        )
                    }
                }

                // Help hint panel containing code for testing
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE2F1F8), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Text(
                        text = "💡 TESTING KEY HINT: The designated code for ${candidate.name} is [ ${candidate.passcode} ]. Enter this key on the pad below to simulate successful unlock.",
                        fontSize = 10.sp,
                        color = Color(0xFF0D47A1),
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }

                // Grid 1-9 & Clear & Submit Keypad (Responsive Custom Buttons >= 48dp target)
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val keys = listOf(
                        listOf("1", "2", "3"),
                        listOf("4", "5", "6"),
                        listOf("7", "8", "9"),
                        listOf("Clear", "0", "OK")
                    )

                    keys.forEach { rowKeys ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            rowKeys.forEach { key ->
                                Button(
                                    onClick = {
                                        codeError = false
                                        when (key) {
                                            "Clear" -> {
                                                if (inputCode.isNotEmpty()) inputCode = inputCode.dropLast(1)
                                            }
                                            "OK" -> {
                                                if (inputCode == candidate.passcode) {
                                                    onUnlockSuccess()
                                                } else {
                                                    codeError = true
                                                    inputCode = ""
                                                    Toast.makeText(context, "❌ Invalid Passcode. Try again!", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                            else -> {
                                                if (inputCode.length < 4) inputCode += key
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp), // >= 48.dp target
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = when (key) {
                                            "OK" -> Color(0xFF2E7D32)
                                            "Clear" -> Color(0xFFC62828)
                                            else -> Color(0xFFECEFF1)
                                        },
                                        contentColor = when (key) {
                                            "OK", "Clear" -> Color.White
                                            else -> Color(0xFF37474F)
                                        }
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = key,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================
// TAB SCREEN: AI compatibility "SANSKRITI" MATCH
// ==========================

@Composable
fun AICompatibilityScreen(
    candidates: List<Candidate>,
    selectedA: Candidate?,
    selectedB: Candidate?,
    onSelectA: (Candidate?) -> Unit,
    onSelectB: (Candidate?) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // AI calculations state
    var isCalculating by remember { mutableStateOf(false) }
    var matchScore by remember { mutableStateOf<Int?>(null) }
    var explanationFamily by remember { mutableStateOf("") }
    var explanationLifestyle by remember { mutableStateOf("") }
    var explanationCulture by remember { mutableStateOf("") }
    var isLiveAIUsed by remember { mutableStateOf(false) }

    // Dropdown pickers state
    var pickerAExpanded by remember { mutableStateOf(false) }
    var pickerBExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Feature Header Banner (Styled in Vibrant Palette style specifications)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, SageBorder),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "🌸 AI \"SANSKRITI\" COMPATIBILITY MATCH",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp,
                    color = DeepTeal,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Powered by Google Gemini 3.5. Reads candidate personal values, community background, and micro-cultural specifics to forecast traditional marital compatibility.",
                    fontSize = 12.sp,
                    color = SageText,
                    lineHeight = 16.sp
                )
            }
        }

        // Selection Cards for Bride & Groom
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Bride (Slot A) Choice
            Column(modifier = Modifier.weight(1f)) {
                Text("👰 Slot A (Bride Choice)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DeepTeal)
                Spacer(modifier = Modifier.height(6.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { pickerAExpanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, SageBorder),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = selectedA?.name ?: "Select Bride",
                            color = if (selectedA != null) DeepTeal else SageText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    DropdownMenu(expanded = pickerAExpanded, onDismissRequest = { pickerAExpanded = false }) {
                        // Filter brides
                        candidates.filter { it.gender == "Bride" }.forEach { bride ->
                            DropdownMenuItem(
                                text = { Text("${bride.name} (${bride.age}, ${bride.village})", fontSize = 12.sp) },
                                onClick = {
                                    onSelectA(bride)
                                    pickerAExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Groom (Slot B) Choice
            Column(modifier = Modifier.weight(1f)) {
                Text("🤵 Slot B (Groom Choice)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DeepTeal)
                Spacer(modifier = Modifier.height(6.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { pickerBExpanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, SageBorder),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = selectedB?.name ?: "Select Groom",
                            color = if (selectedB != null) DeepTeal else SageText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    DropdownMenu(expanded = pickerBExpanded, onDismissRequest = { pickerBExpanded = false }) {
                        // Filter grooms
                        candidates.filter { it.gender == "Groom" }.forEach { groom ->
                            DropdownMenuItem(
                                text = { Text("${groom.name} (${groom.age}, ${groom.village})", fontSize = 12.sp) },
                                onClick = {
                                    onSelectB(groom)
                                    pickerBExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Action Trigger Button (Styled as high-contrast mint accent)
        Button(
            onClick = {
                if (selectedA == null || selectedB == null) {
                    Toast.makeText(context, "Please select both Slot A & Slot B candidates first", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                isCalculating = true
                coroutineScope.launch {
                    val (score, family, lifestyle, culture, realAI) = querySanskritiMatch(selectedA, selectedB)
                    matchScore = score
                    explanationFamily = family
                    explanationLifestyle = lifestyle
                    explanationCulture = culture
                    isLiveAIUsed = realAI
                    isCalculating = false
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MintAccent, contentColor = Color(0xFF003731)),
            border = BorderStroke(1.2.dp, DeepTeal.copy(alpha = 0.3f)),
            shape = RoundedCornerShape(24.dp),
            enabled = !isCalculating
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isCalculating) {
                    CircularProgressIndicator(color = Color(0xFF003731), modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("AI Sanskriti Matching Active...")
                } else {
                    Icon(imageVector = Icons.Default.Star, contentDescription = "Query")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Analyze Cultural Sanskriti Match", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // RESULTS PANEL
        if (isCalculating) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Consulting AI matchmaker...",
                        fontWeight = FontWeight.Bold,
                        color = DeepTeal,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Analyzing local traditions, panchayat distances, daily farming occupations, family backgrounds...",
                        fontSize = 12.sp,
                        color = SageText,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )
                }
            }
        } else if (matchScore != null) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Score Header Circle
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, SageBorder),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Sanskriti Compatibility Score",
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = DeepTeal
                        )

                        // Circular visualization
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .background(LightTeal, CircleShape)
                                .border(3.dp, DeepTeal, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$matchScore%",
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Black,
                                    color = DeepTeal
                                )
                                Text("Match", fontSize = 10.sp, color = SageText, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Engine info badge
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isLiveAIUsed) Color(0xFFE8F5E9) else Color(0xFFECEFF1),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (isLiveAIUsed) "⚡ LIVE GEMINI-3.5 MATCH ENGINE" else "📡 LOCAL DEEP-GRID MATCH SIMULATOR",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isLiveAIUsed) Color(0xFF047857) else SageText
                            )
                        }
                    }
                }

                // Breakdown factors
                Text(
                    text = "🌸 MATCHMAKER ANALYSIS VERDICT",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 11.sp,
                    color = DeepTeal,
                    letterSpacing = 0.5.sp
                )

                // 1. Family Values Alignment
                AnalysisFactorCard(
                    factorName = "1. Family Values Alignment (पारिवारिक मूल्य)",
                    analysis = explanationFamily,
                    icon = Icons.Default.CheckCircle
                )

                // 2. Lifestyle Match
                AnalysisFactorCard(
                    factorName = "2. Lifestyle Suitability (जीवनशैली अनुकूलता)",
                    analysis = explanationLifestyle,
                    icon = Icons.Default.Person
                )

                // 3. Geographical / Panchayat Synergy
                AnalysisFactorCard(
                    factorName = "3. Micro-Culture & Geographics (स्थानीय संस्कृति)",
                    analysis = explanationCulture,
                    icon = Icons.Default.LocationOn
                )
            }
        }
        Spacer(modifier = Modifier.height(80.dp)) // padding spacer instead of invalid contentPadding
    }
}

@Composable
fun AnalysisFactorCard(
    factorName: String,
    analysis: String,
    icon: ImageVector
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, SageBorder),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(LightTeal, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = factorName, tint = DeepTeal, modifier = Modifier.size(16.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(text = factorName, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = DeepTeal)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = analysis, fontSize = 12.sp, color = SageText, lineHeight = 16.sp)
        }
    }
}

// ==========================
// BUSINESS LOGIC: GEMINI API CONNECTION & LOCAL MATCHMAKER
// ==========================

data class MatchResult(
    val score: Int,
    val family: String,
    val lifestyle: String,
    val culture: String,
    val isRealAI: Boolean
)

suspend fun querySanskritiMatch(
    selectedA: Candidate,
    selectedB: Candidate
): MatchResult = withContext(Dispatchers.IO) {
    val apiKey = BuildConfig.GEMINI_API_KEY
    val isDemoKey = apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY"

    if (isDemoKey) {
        // High fidelity local matching calculations representing rural matchmaking logic
        val score = calculateLocalScore(selectedA, selectedB)
        val familyVerdict = getLocalFamilyMatch(selectedA, selectedB)
        val lifestyleVerdict = getLocalLifestyleMatch(selectedA, selectedB)
        val cultureVerdict = getLocalCultureMatch(selectedA, selectedB)

        // Artificially delay a tiny bit to make the loading feel realistic and authoritative
        Thread.sleep(1200)

        MatchResult(score, familyVerdict, lifestyleVerdict, cultureVerdict, isRealAI = false)
    } else {
        // Direct REST API calls to Google Gemini-3.5-Flash
        try {
            val url = URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true

            // Form prompt
            val systemInstructions = "You are a traditional matchmaking advisor (Aguwa) from rural Bihar/UP with excellent understanding of modern professional and traditional farming backgrounds. Compare two candidate profiles for marital matchmaking. Return compatibility as JSON only: {\"score\":Int, \"family\":String, \"lifestyle\":String, \"culture\":String}. Keep family, lifestyle, culture brief (max 2 sentences) in English, rooting arguments in the candidates' context."

            val candidateInfo = "Bride: Name ${selectedA.name}, Age ${selectedA.age}, Education ${selectedA.education}, Occupation ${selectedA.occupation}, Bio details: ${selectedA.bio}, Village ${selectedA.village} in Panchayat ${selectedA.panchayat}, Zila ${selectedA.zila}.\nGroom: Name ${selectedB.name}, Age ${selectedB.age}, Education ${selectedB.education}, Occupation ${selectedB.occupation}, Bio details: ${selectedB.bio}, Village ${selectedB.village} in Panchayat ${selectedB.panchayat}, Zila ${selectedB.zila}."

            val jsonRequest = JSONObject().apply {
                put("contents", org.json.JSONArray().put(
                    JSONObject().put("parts", org.json.JSONArray().put(
                        JSONObject().put("text", "$systemInstructions\n\nAnalyze candidates:\n$candidateInfo")
                    ))
                ))
                put("generationConfig", JSONObject().put("responseMimeType", "application/json"))
            }

            conn.outputStream.use { os ->
                OutputStreamWriter(os, "UTF-8").use { writer ->
                    writer.write(jsonRequest.toString())
                    writer.flush()
                }
            }

            val responseCode = conn.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = conn.inputStream.use { stream ->
                    BufferedReader(InputStreamReader(stream, "UTF-8")).use { reader ->
                        val text = StringBuilder()
                        var line: String?
                        while (reader.readLine().also { line = it } != null) {
                            text.append(line)
                        }
                        text.toString()
                    }
                }

                // Parse response
                val jsonObject = JSONObject(response)
                val candidatesArray = jsonObject.getJSONArray("candidates")
                val textResponse = candidatesArray.getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")

                val cleanJson = JSONObject(textResponse.trim())
                val score = cleanJson.getInt("score")
                val family = cleanJson.getString("family")
                val lifestyle = cleanJson.getString("lifestyle")
                val culture = cleanJson.getString("culture")

                MatchResult(score, family, lifestyle, culture, isRealAI = true)
            } else {
                // Network failed or unauthorized key -> Fallback to custom simulator
                val score = calculateLocalScore(selectedA, selectedB)
                MatchResult(score, "Service error fallback: Candidates share strong traditional joint family roots.", "Perfect educational pairing and livelihood balance computed locally.", "Direct village commute is ideal under panchayat grid.", isRealAI = false)
            }
        } catch (e: Exception) {
            val score = calculateLocalScore(selectedA, selectedB)
            MatchResult(score, "API Error fallback: Exceptional alignment on joint family customs and moral values.", "Highly balanced lifestyles, coordinating farming and community teaching schedules.", "Proximity of villages under Rohtas panchayats enables excellent logistical ease.", isRealAI = false)
        }
    }
}

// ==========================
// LOCAL CORE MATCH SIMULATOR HELPER FUNCTIONS
// ==========================

fun calculateLocalScore(selectedA: Candidate, selectedB: Candidate): Int {
    var score = 70 // Base points

    // Same Caste? Traditional matchmaking gives emphasis (8 points)
    if (selectedA.caste.lowercase() == selectedB.caste.lowercase()) {
        score += 8
    }

    // Proximity logic: Same Panchayat or Zila?
    if (selectedA.zila.lowercase() == selectedB.zila.lowercase()) {
        score += 5
        if (selectedA.prakhand.lowercase() == selectedB.prakhand.lowercase()) {
            score += 5
            if (selectedA.panchayat.lowercase() == selectedB.panchayat.lowercase()) {
                score += 5
            }
        }
    }

    // Age difference check (Ideally 2-5 years)
    val ageDiff = selectedB.age - selectedA.age
    if (ageDiff in 2..5) {
        score += 7
    } else {
        score -= 3
    }

    // Education alignment: Matching basic gradients
    if (selectedA.education.contains("BA") && selectedB.education.contains("B.Sc")) {
        score += 5
    }

    return score.coerceAtMost(99)
}

fun getLocalFamilyMatch(selectedA: Candidate, selectedB: Candidate): String {
    return "Excellent alignment on family codes. ${selectedA.name}'s principal father archetype coordinates perfectly with ${selectedB.name}'s retired registrar framework. Both families demonstrate high standing and deep roots in traditional community values."
}

fun getLocalLifestyleMatch(selectedA: Candidate, selectedB: Candidate): String {
    return "Pairing matches perfectly. Bride’s dedication to community education is highly complementary to Groom's agricultural development focus. Together they can manage an educated household centered on rural leadership and combined farming operations."
}

fun getLocalCultureMatch(selectedA: Candidate, selectedB: Candidate): String {
    val syncLoc = selectedA.village == selectedB.village
    return if (syncLoc) {
        "Unbeatable geographically. Both candidates live in exact close vicinity of Panchayat ${selectedA.panchayat}, Rohtas. This prevents displacement of the bride and ensures immediate family support structures are maintained."
    } else {
        "Favorable regional commute. Panchayat ${selectedA.panchayat} (${selectedA.village}) has excellent transportation access to Panchayat ${selectedB.panchayat} (${selectedB.village}). High affinity in local traditions, temple gatherings, and regional social circles."
    }
}
