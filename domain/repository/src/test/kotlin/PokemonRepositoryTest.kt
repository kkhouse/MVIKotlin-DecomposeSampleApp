
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.flatMap
import com.badoo.reaktive.observable.observable
import com.badoo.reaktive.observable.subscribe
import com.badoo.reaktive.scheduler.overrideSchedulers
import com.badoo.reaktive.single.singleOf
import com.badoo.reaktive.test.scheduler.TestScheduler
import com.badoo.reaktive.test.single.assertSuccess
import com.badoo.reaktive.test.single.test
import com.example.database.PokemonDataBaseInterface
import com.example.model.BaseStats
import com.example.model.Pokemon
import com.example.model.PokemonType
import com.example.model.ProcessResult
import com.example.model.type.PokemonsEnumModel
import com.example.network.GetPokemonQuery
import com.example.network.PokemonService
import com.example.network.type.PokemonEnum
import com.example.repository.PokemonRepositoryImpl
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import org.junit.Test

class PokemonRepositoryTest {
    private val db = mockk<PokemonDataBaseInterface>()
    private val api = mockk<PokemonService>()

    private val repository = PokemonRepositoryImpl(db = db, api = api, )

    val dummyPokemonType = GetPokemonQuery.Type(name = "Electric")

    val dummyBaseStats = GetPokemonQuery.BaseStats(
        attack = 50,
        defense = 40,
        hp = 60,
        specialattack = 90,
        specialdefense = 80,
        speed = 110
    )

    val dummyPokemon = GetPokemonQuery.GetAllPokemon(
        key = PokemonEnum.abra,
        species = "Pikachu",
        sprite = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/25.png",
        types = listOf(dummyPokemonType),
        baseStats = dummyBaseStats,
        baseStatsTotal = dummyBaseStats.attack + dummyBaseStats.defense + dummyBaseStats.hp +
                dummyBaseStats.specialattack + dummyBaseStats.specialdefense + dummyBaseStats.speed,
        num = 1
    )

    val dummyPokemonTypeModel = PokemonType(name = "Electric")

    val dummyBaseStatsModel = BaseStats(
        attack = 50,
        defense = 40,
        hp = 60,
        specialattack = 90,
        specialdefense = 80,
        speed = 110
    )

    val dummyPokemonModel = Pokemon(
        key = PokemonsEnumModel.abra,
        species = "Pikachu",
        sprite = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/25.png",
        types = listOf(dummyPokemonTypeModel),
        baseStats = dummyBaseStatsModel,
        baseStatsTotal = dummyBaseStats.attack + dummyBaseStats.defense + dummyBaseStats.hp +
                dummyBaseStats.specialattack + dummyBaseStats.specialdefense + dummyBaseStats.speed,
        num = 1
    )

    @Test
    fun testGetPokemonResult() {
        overrideSchedulers(
            main = { TestScheduler() },
            io = { TestScheduler() }
        )
        val dummy = singleOf(
            ProcessResult.Success(
                listOf(dummyPokemon)
            )
        )
        every { api.getAllPokemon() } returns dummy
        val result = repository.getPokemonResult()
        result.test().assertSuccess()
        assertEquals(
            ProcessResult.Success(listOf(dummyPokemonModel)),
            result.test().value
        )
    }


    @Test
    fun sortTest() {
        data class A(val num: Int)
        val list = listOf(A(1),A(3),A(-2),A(-1),A(2))
        val minusList = list.filter { it.num < 0 }.sortedBy { it.num }.reversed()
        val plusList = list.filter { it.num >= 0 }.sortedBy { it.num }

        assertEquals(
            listOf(A(1),A(2),A(3),A(-1),A(-2)),
            list.sortedBy { it.num }
        )
    }


    class observableTest {

        data class A(val num: Int)
        data class B(val num: Int)
        fun getObservableA(): Observable<A> {
            return observable { A(num = 1) }
        }

        fun getObservableB(num: Int): Observable<B> {
            return observable { B(num = num) }
        }

        @Test
        fun anyTest() {
            val observableA = getObservableA()
            val observableB = observableA.flatMap {
                getObservableB(it.num*2)
            }
            observableB.subscribe {
                assertEquals(
                    2,
                    it.num
                )
            }
        }
    }

}