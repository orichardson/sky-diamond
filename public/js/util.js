const removeFrom = function(arr, x) {
    let idx = arr.indexOf(x);
    if (idx > 0) {
        arr.splice(idx, 1);
        return 1 + removeFrom(arr, x);
    }
    return 0;
};

const flatMap = (f, xs) =>
    xs.map(f).reduce( (x,y) =>  x.concat(y), []);


function union(setA, setB) {
    var union = new Set(setA);
    for (var elem of setB) {
        union.add(elem);
    }
    return union;
}


paper.Color.prototype.interp = function(other, amt) {
    return new paper.Color(Vec.interp(this.components, other.components, amt));
};


paper.Color.prototype.brighter = function(amt) {
    return this.components.map( v => v  + (1-v)*amt )
};

paper.Color.prototype.darker = function(amt) {
    return this.components.map( v => v  + (0-v)*amt )
};
