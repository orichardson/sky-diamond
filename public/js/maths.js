(function(global) {
    /* VECTOR SPACES ON ALGEBRA */
    let merge = function(f) {
        return function(a,b) {
            if (a === undefined)
                return a;

            let toRet = [];
            for (let i = 0; i < a.length; i++)
                toRet.push(f(a[i], b[i]))
            return toRet;
        }
    };

    function sum(a) {
        let total = 0;
        for(i = 0; i < a.length; i++) {
            total += a[i]
        }
        return total;
    }

    let plus = merge( (x,y) => x+y );
    let minus = merge( (x,y) => x-y );

    function neg(a) {
        if(a === undefined)
            return a;

        let toRet = [];
        for(let i = 0; i < a.length; i++)
            toRet.push(- a[i])
        return toRet;
    }

    function scale(a, s) {
        let toRet = [];
        for(let i = 0; i < a.length; i++)
            toRet.push(a[i] * s)
        return toRet;

    }
    function norm(a) {
        return Math.sqrt(sum(a.map(x => x*x)));
    }

    function dot(a,b) {
        return sum(merge( (x,y) => x*y )( a, b ))
    }

    function dist(a, b) {
        let toRet = 0;
        for(let i = 0; i < a.length; i++)
            toRet += (a[i] - b[i])**2;

        return Math.sqrt(toRet);
    }

    function interp(a, b, amt) {
        let toRet = [];
        for(let i = 0; i < a.length; i++)
            toRet.push(a[i] * (1-amt) + b[i] * amt)
        return toRet;
    }

    function normalized(a) {
        let n = norm(a);
        if(n === 0) return a;
        return scale(a, 1/n);
    }


    Array.prototype.plus = function(x) { return plus(this,x); };
    Array.prototype.minus = function(x) { return plus(this,x); };

    global.Vec = {plus : plus, minus : minus, neg: neg, dist: dist, interp: interp, normalized : normalized, scale : scale, norm : norm, dot:dot};



    /*************** GEOMETRIC ALGEBRA ********************/



    global.GA = {

    }

})(this);

