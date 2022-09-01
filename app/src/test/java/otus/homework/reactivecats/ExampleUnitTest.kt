package test.java.otus.homework.reactivecats

import android.app.Application
import android.util.Log
import io.mockk.every
import io.mockk.mockk
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.TestScheduler
import io.reactivex.schedulers.Timed
import io.reactivex.subscribers.TestSubscriber
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import otus.homework.reactivecats.Fact
import otus.homework.reactivecats.LocalCatFactsGenerator
import java.util.concurrent.TimeUnit


/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {

    private val mockApp = mockk<Application>(relaxed = true)

    @Before
    fun preperation() {
        every { mockApp.resources.getStringArray(any()) } returns arrayOf(
            "Cats have 230 bones, while humans only have 206.",
            "Cats have whiskers on the backs of their front legs, as well.",
            "Some cats are ambidextrous, but 40 percent are either left- or right-pawed.",
            "Cats live longer when they stay indoors.",
            "Cats can spend up to a third of their waking hours grooming.",
            "Cats will refuse an unpalatable food to the point of starvation.",
        )
    }

    @Test
    fun `generate Random Cat Fact`() {
        val result = mutableListOf<Fact>()
        val arrayOfFacts = mockApp.resources.getStringArray(2)
        val subscribe = LocalCatFactsGenerator(mockApp).generateCatFact()
            .subscribe(result::add)
        assertTrue(result.size == 1)
        assertTrue(arrayOfFacts.contains(result.first().text))
    }

    @Test
    fun `generate random cat facts periodically`() {
        val scheduler = TestScheduler()
        val subscriber = TestSubscriber<Timed<Fact>>()
        val arrayOfFacts = mockApp.resources.getStringArray(2)
        fun checkIfContain(inputFact: Fact): Boolean = arrayOfFacts.contains(inputFact.text)
        fun Map<Long, Fact>.doesntHaveDuplicatesAroundItems() = values.withIndex().toList().let { list ->
            list.all {
                val previous = list.getOrNull(it.index - 1)?.value?.text
                val actual = it.value.text
                val next = list.getOrNull(it.index + 1)?.value?.text
                if (previous != null && next != null)
                    previous != actual && actual != next
                else if (previous == null)
                    actual != next
                else if (next == null)
                    previous != next
                else
                    true
            }
        }

        //Настраиваем стандартный Scheduler, чтобы все слушали наши изменения во времени
        RxJavaPlugins.setComputationSchedulerHandler{ scheduler }

        val subscribe = LocalCatFactsGenerator(mockApp).generateCatFactPeriodically()
            .observeOn(scheduler)
            .subscribeOn(scheduler)
            //Добавляем время, чтобы отслеживать периодичность
            .timestamp(TimeUnit.MILLISECONDS)
            .take(5)
            .subscribe(subscriber)

        subscriber.assertNoValues()
        subscriber.assertNotComplete()

        //Имитируем ожидаение в 1 секунду
        scheduler.advanceTimeBy(1, TimeUnit.SECONDS)

        subscriber.assertNoErrors()
        subscriber.assertNoValues()

        //Имитируем ожидаение в 1 секунду
        scheduler.advanceTimeBy(1, TimeUnit.SECONDS)

        subscriber.assertNoErrors()
        //Проверяем, что элементов ровно 1
        assertSame(1, subscriber.valueCount())
        //Проверяем, что все элементы из оригинальной коллекции
        subscriber.assertValue {
            checkIfContain(it.value())
        }

        //Имитируем ожидаение в 50 секунд, чтобы однозначно завершить выдачу
        scheduler.advanceTimeTo(50, TimeUnit.SECONDS)

        subscriber.cancel()

        subscriber.assertNoErrors()
        //Проверяем, что элементы вокруг каждого из элементов уникальны
        assert(subscriber.values().associate { it.time() to it.value() }.doesntHaveDuplicatesAroundItems())
        //Проверяем, что каждая выдача была с 2х-секундным интервалом
        //Так как элементы могут пропускаться, то просто проверяем кратность интервалов
        assertTrue(subscriber.values().all { it.time() % 2 == 0L })
    }
}