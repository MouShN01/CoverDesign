package com.example.coverdesign.ui.screens.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.coverdesign.R


@Composable
fun GoogleSignInButton(
    isLoading : Boolean,
    onClick   : () -> Unit,
    modifier  : Modifier = Modifier
) {
    OutlinedButton(
        onClick  = onClick,
        enabled  = !isLoading,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        shape    = RoundedCornerShape(10.dp),
        border   = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        if (isLoading) {
            CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
        } else {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Image(
                    painter            = painterResource(R.drawable.ic_google_logo),
                    contentDescription = null,
                    modifier           = Modifier.size(20.dp)
                )
                Text(
                    "Sign in with Google",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}
