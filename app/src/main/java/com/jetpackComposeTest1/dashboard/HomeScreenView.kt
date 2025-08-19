package com.jetpackComposeTest1.dashboard

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jetpackComposeTest1.R
import com.jetpackComposeTest1.ui.theme.main_appColor
import com.jetpackComposeTest1.utils.Utils

@Composable
fun HomeScreenView() {


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = main_appColor),
        contentAlignment = Alignment.TopCenter
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Centered Text
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "GoGoGreen",
                        style = TextStyle(
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    )
                }

                // Icon at the end
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    modifier = Modifier.size(28.dp),
                    tint = Color.White
                )
            }



            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(
                        RoundedCornerShape(
                            topStart = 24.dp,
                            topEnd = 24.dp,
                            bottomStart = 0.dp,
                            bottomEnd = 0.dp
                        )
                    )
                    .background(Color.White)
            ) {

                Column(modifier = Modifier.padding(10.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                            .clip(
                                RoundedCornerShape(
                                    topStart = 24.dp,
                                    topEnd = 24.dp,
                                    bottomStart = 24.dp,
                                    bottomEnd = 24.dp
                                )
                            )
                            .height(150.dp)

                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_banner_img),
                            contentDescription = "My Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly // spaces cards evenly
                    ) {
                        repeat(4) { index ->
                            Card(
                                modifier = Modifier
                                    .size(80.dp) // card size
                                    .padding(4.dp),
                                shape = RoundedCornerShape(12.dp),
                                elevation = CardDefaults.cardElevation(6.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White
                                )
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {

                                    Image(
                                        painter = painterResource(
                                            id = when (index) {
                                                0 -> R.drawable.flower1
                                                1 -> R.drawable.flower2
                                                2 -> R.drawable.gift
                                                else -> R.drawable.love
                                            }
                                        ),
                                        contentDescription = "My Image ${index}",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(10.dp),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    )
                    {
                        Text(
                            text = "Popular Items",
                            style = TextStyle(
                                color = Color.Black,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        )
                        Text(
                            text = "View All",
                            style = TextStyle(
                                color = main_appColor,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        )
                    }

                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp) // spacing between items
                    ) {
                        items(Utils.popularItemsList.size) { index ->
                            Card(
                                modifier = Modifier
                                    .width(120.dp).height(170.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                            ) {

                                Column (
                                    modifier = Modifier.fillMaxSize().padding(10.dp),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Image(
                                        painter = painterResource(
                                            id = Utils.popularItemsList[index].icon
                                        ),
                                        contentDescription = "Flower ${index}",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(100.dp)
                                            .padding(10.dp),
                                        contentScale = ContentScale.Crop
                                    )
                                    Text(modifier = Modifier.fillMaxWidth(), text = Utils.popularItemsList[index].name, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    Text(modifier = Modifier.fillMaxWidth(),text = "$${Utils.popularItemsList[index].price}", fontSize = 14.sp, fontWeight = FontWeight.Bold, style = TextStyle(color = main_appColor))

                                }
                            }
                        }
                    }
                }


            }
        }
    }
}


@Preview()
@Composable
fun HomeScreenPreview() {
    HomeScreenView()
}