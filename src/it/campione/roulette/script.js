// Funzione per aggiornare la barra inferiore
function aggiornaHistoryBar(numero, colore) {
  const historyBar = document.querySelector('.history-bar');
  const newItem = document.createElement('div');
  newItem.classList.add('history-item');
  newItem.classList.add(colore);
  newItem.textContent = numero;
  const animationToggle = document.getElementById('animationToggle');

  if (historyBar.firstChild) {
    historyBar.insertBefore(newItem, historyBar.firstChild);
  } else {
    historyBar.appendChild(newItem);
  }

  // Rimuovi l'ultimo elemento se ce ne sono più di 20, senza animazione
  if (historyBar.childElementCount > 20) {
    const lastItem = historyBar.lastChild;
    historyBar.removeChild(lastItem);
  }

  if (animationToggle.checked) {
    // Forza il layout per garantire che l'animazione di comparsa sia applicata
    newItem.offsetHeight;
    newItem.style.opacity = 1;
    newItem.classList.add('show');
  } else {
    newItem.classList.add('no-animation');
  }
}

document.getElementById('spinButton').addEventListener('click', function() {
    const numbers = document.querySelectorAll('.number');
    const ball = document.getElementById('ball');
    const result = document.getElementById('result');
    const wheel = document.querySelector('.roulette-wheel');
    const animationToggle = document.getElementById('animationToggle');

    if (animationToggle.checked) {
        // Aggiungi l'animazione alla ruota
        wheel.classList.add('spinning');
        // Rimuovi l'animazione dopo 2 secondi
        setTimeout(function() {
            wheel.classList.remove('spinning');
        }, 2000);
    }

    // Genera un numero casuale tra 0 e 36
    const randomIndex = Math.floor(Math.random() * numbers.length);
    const selectedNumber = numbers[randomIndex].dataset.number;

    // Posiziona la pallina sul numero estratto
    const selectedElement = numbers[randomIndex];
    const rect = selectedElement.getBoundingClientRect();
    const wheelRect = document.querySelector('.roulette-wheel').getBoundingClientRect();
    ball.style.top = `${rect.top - wheelRect.top + rect.height / 2}px`;
    ball.style.left = `${rect.left - wheelRect.left + rect.width / 2}px`;

    // Rimuovi eventuali effetti "glow" precedenti
    numbers.forEach(number => number.classList.remove('glow'));

    // Aggiungi l'effetto "glow" al numero estratto
    selectedElement.classList.add('glow');

    // Determina il colore, la parità e l'intervallo del numero estratto
    let color, parity, range;
    if (selectedNumber == 0) {
        color = 'verde';
        parity = 'n/a';
        range = 'n/a';
    } else {
        color = selectedElement.classList.contains('red') ? 'rosso' : 'nero';
        parity = selectedNumber % 2 === 0 ? 'pari' : 'dispari';
        range = selectedNumber <= 18 ? 'basso' : 'alto';
    }

    const selectedColor = (color === 'verde') ? 'green' : (color === 'rosso') ? 'red' : 'black';
    aggiornaHistoryBar(selectedNumber, selectedColor);

    // Mostra il risultato
    result.textContent = `Numero estratto: ${selectedNumber} (${color}, ${parity}, ${range})`;

    if (animationToggle.checked) {
        // Aggiungi l'animazione di sfondo verde
        result.classList.add('flash-background');
        // Rimuovi l'animazione di sfondo dopo un secondo
        setTimeout(function() {
            result.classList.remove('flash-background');
        }, 1000);
    }
});