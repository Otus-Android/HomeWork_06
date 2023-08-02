# ReactiveCats

## Задача

1. Переведите сетевой запрос с `retrofit.Call` на RX цепочку. Для этого подключите Retrofit адаптер, поменяйте возвращаемые типы функций

2. Поменяйте логику в `CatsViewModel.kt` с колбеков на RX. Логику обработки успеха/ошибки из коллбека необходимо перенести в терминальные коллбеки RX цепочки. Не забудьте очистить подписки когда `ViewModel` уничтожается

3. Реализуйте функцию `otus.homework.reactivecats.LocalCatFactsGenerator#generateCatFact`, так, чтобы она возвращала `Fact` со случайной строкой  из массива строк `R.array.local_cat_facts` обернутую в подходящий стрим(`Flowable`/`Single`/`Observable` и т.п)

4. Реализуйте функцию `otus.homework.reactivecats.LocalCatFactsGenerator#generateCatFactPeriodically` так, чтобы она эмитила `Fact` со случайной строкой из массива строк `R.array.local_cat_facts` каждые 2000 миллисекунд. Если вновь заэмиченный Fact совпадает с предыдущим - пропускаем элемент.

5. Реализуйте функцию `otus.homework.reactivecats.CatsViewModel#getFacts` следующим образом:  каждые 2 секунды идем в сеть за новым фактом, если сетевой запрос завершился неуспешно, то в качестве фоллбека идем за фактом в уже реализованный `otus.homework.reactivecats.LocalCatFactsGenerator#generateCatFact`.


## Решение

Приложение переведено на использование `RxJava3`. 

Реализован MVI паттерн на основе модифицированного варианта библиотеки `Roxie` (см. `MviViewModel`).
Реализация `Roxie` использует `RxJava2`, `MviViewModel` построена на `RxJava3` с дополнительными изменениями.

`LocalCatFactsGenerator` преобразован в `CatsStubStorage` и используется в качестве локального источника данных.

`CatsViewModel` периодически обновляет информацию факта о коте.