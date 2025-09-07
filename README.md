# ReactiveCats

## Задание: Миграция с колбеков на RxJava2

### Что нужно сделать:

1. **Подключить RxJava2 к Retrofit**
   - Добавить `retrofit2:adapter-rxjava2` в зависимости
   - Изменить `CatsService.getCatFact()` чтобы возвращал `Single<Fact>` вместо `Call<Fact>`

2. **Переписать CatsViewModel на RxJava2**
   - Убрать колбеки из `init` блока
   - Реализовать `getFacts()` с помощью RxJava2
   - Использовать `CompositeDisposable` для управления подписками, не забыть отписаться

3. **Реализовать LocalCatFactsGenerator.generateCatFact()**
   - Возвращать `Single<Fact>` со случайным фактом из `R.array.local_cat_facts`
   - Использовать `Single.fromCallable()` или `Single.just()`

4. **Реализовать LocalCatFactsGenerator.generateCatFactPeriodically()**
   - Эмитить `Fact` каждые 2 секунды
   - Пропускать дублирующиеся факты

5. **Реализовать CatsViewModel.getFacts()**
   - Каждые 2 секунды запрашивать факт из сети
   - При ошибке сети - использовать `LocalCatFactsGenerator.generateCatFact()` как фоллбек
   - Использовать `onErrorResumeNext()` для обработки ошибок

### Подсказки:
- `Single` - для одноразовых операций
- `Flowable` - для периодических/непрерывных потоков
- `CompositeDisposable` - для управления подписками
- `distinctUntilChanged()` - для исключения дубликатов
- `onErrorResumeNext()` - для фоллбека при ошибках

### Что проверить:
- Факты обновляются каждые 2 секунды
- При отключенном интернете показываются локальные факты
- Нет дублирующихся фактов подряд
