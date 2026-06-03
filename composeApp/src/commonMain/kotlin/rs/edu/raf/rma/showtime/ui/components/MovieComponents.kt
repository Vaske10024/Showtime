package rs.edu.raf.rma.showtime.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import rs.edu.raf.rma.showtime.domain.MovieSummary
import rs.edu.raf.rma.showtime.util.imageUrl
import rs.edu.raf.rma.showtime.util.ratingText

@Composable
fun MovieRow(
    movie: MovieSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onRemove: (() -> Unit)? = null,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Poster(path = movie.posterPath, modifier = Modifier.width(78.dp).aspectRatio(2f / 3f))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(movie.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(listOfNotNull(movie.year?.toString(), movie.genres.firstOrNull()?.name).joinToString(" | "), style = MaterialTheme.typography.bodyMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(movie.imdbRating.ratingText(), style = MaterialTheme.typography.bodySmall)
                    if (movie.imdbVotes != null) {
                        Spacer(Modifier.width(8.dp))
                        Text("${movie.imdbVotes} votes", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            if (onRemove != null) {
                IconButton(onClick = onRemove) {
                    Icon(Icons.Default.Delete, contentDescription = "Remove")
                }
            }
        }
    }
}

@Composable
fun Poster(path: String?, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        val url = imageUrl(path, size = "w342")
        if (url == null) {
            Text("No image", style = MaterialTheme.typography.labelSmall)
        } else {
            AsyncImage(
                model = url,
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop,
            )
        }
    }
}

@Composable
fun Backdrop(path: String?, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        val url = imageUrl(path, size = "w780")
        if (url == null) {
            Text("No backdrop")
        } else {
            AsyncImage(
                model = url,
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop,
            )
        }
    }
}

@Composable
fun GenreChips(genres: List<rs.edu.raf.rma.showtime.domain.Genre>) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        genres.take(3).forEach { genre ->
            AssistChip(onClick = {}, label = { Text(genre.name) })
        }
    }
}

@Composable
fun CenterMessage(
    message: String,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (isLoading) CircularProgressIndicator()
            if (isLoading) Spacer(Modifier.height(12.dp))
            Text(message, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
