package com.example.network

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.annotations.ApolloExperimental
import com.apollographql.apollo3.mockserver.MockServer
import com.apollographql.apollo3.mockserver.enqueue
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test


internal class PokemonServiceImplTest {
    @Before
    fun before() {
    }

    @OptIn(ApolloExperimental::class)
    @Test
    fun test() {
        runBlocking {
            // Create a mock server
            val mockServer = MockServer()

            // Provide its URL to your ApolloClient
            val apolloClient = ApolloClient.Builder()
                .serverUrl(mockServer.url())
                .build()

            mockServer.enqueue("""{
  "data": {
    "getAllPokemon": [
      {
        "key": "medichammega",
        "species": "medicham-mega",
        "sprite": "https://play.pokemonshowdown.com/sprites/ani/medicham-mega.gif",
        "types": [
          {
            "name": "Fighting"
          },
          {
            "name": "Psychic"
          }
        ],
        "baseStats": {
          "attack": 100,
          "defense": 85,
          "hp": 60,
          "specialattack": 80,
          "specialdefense": 85,
          "speed": 100
        },
        "baseStatsTotal": 510
      },
      {
        "key": "electrike",
        "species": "electrike",
        "sprite": "https://play.pokemonshowdown.com/sprites/ani/electrike.gif",
        "types": [
          {
            "name": "Electric"
          }
        ],
        "baseStats": {
          "attack": 45,
          "defense": 40,
          "hp": 40,
          "specialattack": 65,
          "specialdefense": 40,
          "speed": 65
        },
        "baseStatsTotal": 295
      }
    ]
  }
}""")

            val response = apolloClient
                .query(GetPokemonQuery())
                .execute()

            assertEquals(
                "medichammega",
                response.data?.getAllPokemon?.get(0)?.key?.rawValue
            )
            mockServer.stop()
        }
    }
}