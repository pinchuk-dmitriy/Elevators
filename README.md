# Итоговый проект
1. Есть многоэтажное здание (этажность конфигурируема). В здании есть лифты (количество конфигурируемо). На каждом этаже есть кнопки вызова “вверх” и “вниз” (общие для всех лифтов) На каждом этаже появляются люди (рандомной массы), которые хотят ехать на другой этаж (рандомный). Интенсивность генерации людей конфигурируема
2. У каждого лифта есть грузоподъемность, скорость и скорость открытия/закрытия дверей.
3. У человека есть масса и этаж, на который ему нужно.
4. Люди стоят в очереди на засадку в лифты (одна очередь вверх, одна вниз) не нарушая её. Приехав на нужный этаж, человек исчезает.
# Реализовано непрерывно работающее приложение (люди появляются, вызывают лифт и едут на нужный этаж) используя многопоточность.
- тесты, maven, логгирование;
- реализован сбор статистики (сколько людей перевезено каждым лифтом и другие);
- логирование основных событий системы (чтобы по логам можно было следить за тем, что происходит);