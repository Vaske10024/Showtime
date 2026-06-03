package rs.edu.raf.rma.networking.di

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import org.koin.dsl.module
import rs.edu.raf.rma.core.auth.AuthStore
import rs.edu.raf.rma.core.auth.model.AuthState
import rs.edu.raf.rma.networking.HttpClientFactory
import rs.edu.raf.rma.showtime.data.api.AuthenticationApi
import rs.edu.raf.rma.showtime.data.api.MoviesApi
import rs.edu.raf.rma.showtime.data.api.UserApi

val networkingModule = module {
    single<HttpClient>(Qualifiers.Unauthenticated) {
        HttpClientFactory.createHttpClientWithDefaultConfig()
    }

    single<HttpClient>(Qualifiers.Authenticated) {
        val authStore: AuthStore = get()
        HttpClientFactory.createHttpClientWithDefaultConfig {
            installBearerToken(authStore)
        }
    }

    single { AuthenticationApi(client = get(Qualifiers.Unauthenticated)) }
    single { MoviesApi(client = get(Qualifiers.Unauthenticated)) }
    single { UserApi(client = get(Qualifiers.Authenticated)) }
}

private fun HttpClientConfig<*>.installBearerToken(authStore: AuthStore) {
    defaultRequest {
        when (val authState = authStore.authState.value) {
            is AuthState.Authenticated -> header(
                key = HttpHeaders.Authorization,
                value = "Bearer ${authState.data.accessToken}",
            )
            AuthState.Unauthenticated -> Unit
        }
    }
}
