# ReactiveCats

1. Переведите сетевой запрос с `retrofit.Call` на RX цепочку. Для этого подключите Retrofit 
2. адаптер, поменяйте возвращаемые типы функций

2. Поменяйте логику в `CatsViewModel.kt` с колбеков на RX. Логику обработки успеха/ошибки 
3. из коллбека необходимо перенести в терминальные коллбеки RX цепочки. Не забудьте очистить 
4. подписки когда `ViewModel` уничтожается

3. Реализуйте функцию `otus.homework.reactivecats.LocalCatFactsGenerator#generateCatFact`, 
4. так, чтобы она возвращала `Fact` со случайной строкой  из массива строк `R.array.local_cat_facts` 
5. обернутую в подходящий стрим(`Flowable`/`Single`/`Observable` и т.п)

4. Реализуйте функцию `otus.homework.reactivecats.LocalCatFactsGenerator#generateCatFactPeriodically` 
5. так, чтобы она эмитила `Fact` со случайной строкой из массива строк `R.array.local_cat_facts` 
6. каждые 2000 миллисекунд. Если вновь заэмиченный Fact совпадает с предыдущим - пропускаем элемент.

5. Реализуйте функцию `otus.homework.reactivecats.CatsViewModel#getFacts` следующим образом:  
6. каждые 2 секунды идем в сеть за новым фактом, если сетевой запрос завершился неуспешно, 
7. то в качестве фоллбека идем за фактом в уже реализованный 
8. `otus.homework.reactivecats.LocalCatFactsGenerator#generateCatFact`.
