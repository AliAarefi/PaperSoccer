const canvas = new fabric.Canvas('canvas', {
  selection: false,
  preserveObjectStacking: true,
  backgroundColor: '#eee'
});
fabric.Object.prototype.originX = fabric.Object.prototype.originY = 'center';

let margin_left = 28, margin_top = 25,
  width = 763 - (2 * margin_left), height = 600 - (2 * margin_top);

let rows = 6, columns = 6;  // Both must be even

let ballIndex = (rows / 2) * (columns + 1) + Math.ceil((columns + 1) / 2) - 1;

// Create ball places of the game
let dots = [];
for (let i = 0; i < rows + 1; ++i) {
  for (let j = 0; j < columns + 1; ++j) {
    let dot = new fabric.Circle({
      left: margin_left + (j * (width / columns)),
      top: margin_top + (i * (height / rows)),
      radius: 4,
      hasControls: false,
      selectable: false,
      hoverCursor: 'pointer',
      class: 'ballPlace',
      valid: false
    });
    dots.push(dot);
    canvas.add(dot);
  }
}

// Dots which are valid moves, get bigger when hovered
canvas.on('mouse:over', e => {
  let target = e.target;
  if (target && target.class === 'ballPlace') {
    if (target.valid) {
      target.set('radius', 7);
      canvas.requestRenderAll();
    }
  }
});
canvas.on('mouse:out', e => {
  let target = e.target;
  if (target && target.class === 'ballPlace') {
    if (target.valid) {
      target.set('radius', 4);
      canvas.requestRenderAll();
    }
  }
});

let lines = [];
function addLine(i, j) {
  let line = new fabric.Line([dots[i].left, dots[i].top, dots[j].left, dots[j].top], {
    fill: '#000',
    stroke: '#000',
    strokeWidth: 2,
    evented: false,
    
  });
  lines.push(line);
  canvas.add(line);
}

// Draw board lines
for (let i = 0; i < rows; ++i) {
  addLine(i, i + 1);
  addLine((rows + 1) * columns + i, (rows + 1) * columns + i + 1);
}
for (let j = 0; j < columns; ++j) {
  addLine(j * (columns + 1), j * (columns + 1) + columns + 1);
  addLine(columns + (j * (columns + 1)), columns + ((j + 1) * (columns + 1)));
}
