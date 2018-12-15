const canvas = new fabric.Canvas('canvas', {
  selection: false,
  preserveObjectStacking: true,
  backgroundColor: '#eee'
});
fabric.Object.prototype.originX = fabric.Object.prototype.originY = 'center';

const margin_left = 40, margin_top = 70,
  width = 763 - (2 * margin_left), height = 600 - (2 * margin_top);

let rows = 6, columns = 4;  // Both must be even

let horizontal_distance = width / columns, vertical_distance = height / rows;

function parseAdjacency(s) {
  return s.split('\n').map(row => row.split(' ').map(e => e !== '0'))
}

function PaperSoccerBoard(canvas, rows, columns) {
  this.canvas = canvas;

  this.rows = rows;
  this.columns = columns;
  this.ballIndex = (rows / 2) * (columns + 1) + Math.ceil((columns + 1) / 2) - 1;

  this.dots = [];
  this.validMoves = [];
  for (let i = 0; i < rows + 1; ++i) {
    for (let j = 0; j < columns + 1; ++j) {
      let dot = new fabric.Circle({
        left: margin_left + (j * horizontal_distance),
        top: margin_top + (i * vertical_distance),
        radius: 4,
        hasControls: false,
        selectable: false,
        hoverCursor: 'pointer',
        class: 'ballPlace',
        index: i + j,
        valid: false
      });
      this.dots.push(dot);
      canvas.add(dot);
    }
  }
  this.dots[this.ballIndex].set({radius: 7, fill: '#0F0'});

  this.adjacency = [];
  for (let i = 0; i < (rows) * (columns); ++i) {
    let row = [];
    for (let j = 0; j < (rows) * (columns); ++j)
      row.push(null);
    this.adjacency.push(row);
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

  this.activateValidMoves();

  return this
}

PaperSoccerBoard.prototype.constructor = PaperSoccerBoard;

PaperSoccerBoard.prototype.addLine = async function (i, j) {
  let line = new fabric.Line([dots[i].left, dots[i].top, dots[j].left, dots[j].top], {
    fill: '#000',
    stroke: '#000',
    strokeWidth: 2
  });
  this.adjacency[i][j] = this.adjacency[j][i] = line;
  this.canvas.add(line);
};

PaperSoccerBoard.prototype.diffAdjacency = async function (adj) {
  for (let i = 0; i < this.adjacency.length; ++i) {
    for (let j = 0; j < this.adjacency[i].length; ++j) {
      if (this.adjacency[i][j] === null && adj[i][j]) {
        this.addLine(i, j);
      }
    }
  }
};

PaperSoccerBoard.prototype.getValidMoves = function () {
  let validMoves;
  let ballIndex = this.ballIndex;

  // Corners
  if (ballIndex === 0) validMoves = [1, columns + 1, columns + 2];  // Top left
  else if (ballIndex === columns) validMoves = [columns - 1, ballIndex + (columns + 1), ballIndex + columns];  // Top right
  else if (ballIndex === (columns + 1) * rows + 1) validMoves = [ballIndex + 1, ballIndex - (columns + 1), ballIndex - columns];  // Bottom left
  else if (ballIndex === (rows + 1) * (columns + 1) - 1) validMoves = [ballIndex - 1, ballIndex - (columns + 1), ballIndex - (columns + 2)];  // Bottom right

  // Edges
  else if (ballIndex <= columns) {  // Top edge
    validMoves = [ballIndex - 1, ballIndex + 1, ballIndex + (columns + 1), ballIndex + columns, ballIndex + (columns + 2)];
    if (Math.abs(ballIndex - (columns / 2)) <= 1) validMoves += [-1, -2, -3];  // Goal is reachable
  } else if (ballIndex > (rows * (columns + 1))) {  // Bottom edge
    validMoves = [ballIndex - 1, ballIndex + 1, ballIndex - (columns + 1), ballIndex - columns, ballIndex - (columns + 2)];
    if (Math.abs(ballIndex - (rows * (columns + 1)) - (columns / 2)) <= 1) validMoves += [-4, -5, -6];  // Goal is reachable
  } else if (ballIndex % (columns + 1) === 0) {  // Left edge
    validMoves = [ballIndex + 1, ballIndex - (columns + 1), ballIndex + (columns + 1), ballIndex - columns, ballIndex + (columns + 2)];
  } else if ((ballIndex - 4) % (columns + 1) === 0) {  // Right edge
    validMoves = [ballIndex - 1, ballIndex - (columns + 1), ballIndex + (columns + 1), ballIndex - (columns + 2), ballIndex + columns];
  }

  // No constraint
  else
    validMoves = [ballIndex - 1, ballIndex + 1, ballIndex - (columns + 2), ballIndex - (columns + 1), ballIndex - columns, ballIndex + columns, ballIndex + (columns + 1), ballIndex + (columns + 2)]

  return validMoves.filter(i => this.adjacency[ballIndex][i] === null);
};

PaperSoccerBoard.prototype.activateValidMoves = async function () {
  for (let i of this.validMoves)
    this.dots[i].set('valid', false);
  this.validMoves = this.getValidMoves();
  for (let i of this.validMoves)
    this.dots[i].set('valid', true);
};

PaperSoccerBoard.prototype.move = function (dest) {

};

let board = new PaperSoccerBoard(canvas, rows, columns);
