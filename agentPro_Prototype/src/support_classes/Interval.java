package support_classes;

/**
 * Definiert beschränktes, abgeschlossenes Intervall ganzer Zahlen.
 *
 * @author Klaus Köhler, koehler@hm.edu
 * @author Reinhard Schiedermeier, rs@cs.hm.edu
 * @version 15.06.2008
 */
public class Interval {
    private final long lowerBound;
    private final long size;
    private String id;

    private final long upperBound;

    private final boolean isEmpty;

    /**
     * Erzeugt ein Intervall [l, u].
     * @param l untere Intervallgrenze
     * @param u obere Intervallgrenze
     */
    public Interval(final long l, final long u) {
        this(l, u, u < l);
    }

    /**
     * Erzeugt ein leeres Intervall.
     */
    public Interval() {
        this(0, 0, true);
    }

    public Interval(final Interval i) {
        this(i.lowerBound(), i.upperBound(), i.isEmpty());
    }

    /**
     * Erzeugt ein Intervall [l, u].
     * @param l untere Intervallgrenze
     * @param u obere Intervallgrenze
     * @param e wenn true: leeres Intervall
     */
    public Interval(final long l, final long u, final boolean e) {
        lowerBound = l;
        upperBound = u;
        isEmpty = e;
        size = upperBound - lowerBound;
    }

    public Interval(String startDate, String endDate, boolean b) {
		lowerBound = Long.parseLong(startDate);
		upperBound = Long.parseLong(endDate);
		isEmpty = b;
		size = upperBound - lowerBound;
	}

	/**
     * Stellt fest, ob dieses Intervall leer ist.
     * @return true, wenn leer, sonst false
     */
    public boolean isEmpty() {
        return isEmpty;
    }

    public long lowerBound() {
        return lowerBound;
    }

    public long upperBound() {
        return upperBound;
    }

    @Override
    public String toString() {
        if(isEmpty())
            return "[]";
        return String.format("[%d,%d]", lowerBound(), upperBound());
    }

    @Override
    public boolean equals(final Object x) {
        if(x == null)
            return false;
        if(x.getClass() != Interval.class)
            return false;
        final Interval i = (Interval)x;
        if(isEmpty())
            return i.isEmpty();
        if(lowerBound() != i.lowerBound())
            return false;
        if(upperBound() != i.upperBound())
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        if(isEmpty())
            return 0;
        int hash = 17;
        hash = (int) (47*hash + lowerBound);
        hash = (int) (47*hash + upperBound);
        return hash;
    }

    /**
     * Gibt Auskunft, ob die Zahl i in diesem Intervall enthalten ist.
     * @param i die Zahl
     * @return
     */
    public boolean contains(final long i) {
        if(isEmpty())
            return false;
        return i >= lowerBound()  &&  i <= upperBound();
    }

    /**
     * Gibt Auskunft, ob ein Intervall i Teilmenge dieses Intervalls ist.
     * @param i das zu vergleichende Intervall
     * @return true, wenn i Teilintervall ist, sonst false
     */
    public boolean contains(final Interval i) {
        if(i.isEmpty())
            return true;
        if(isEmpty())
            return false;
        return contains(i.lowerBound())  &&  contains(i.upperBound());
    }

    /**
     * Gibt Auskunft, ob ein Intervall i kein Element mit diesem Intervall gemeinsam hat.
     * @param i das zu vergleichende Intervall
     * @return true, wenn kein gemeinsames Element existiert, sonst false
     */
    public boolean disjoint(final Interval i) {
        return isEmpty()
            ||  i.isEmpty()
            ||  lowerBound() > i.upperBound()
            ||  upperBound() < i.lowerBound();
    }

    /**
     * Liefert die Hülle dieses Intervalls und eines übergebenen Intervalls i.
     * @param i das andere Intervall
     * @return die Hülle
     */
    public Interval hull(final Interval i) {
        if(isEmpty())
            return i;
        if(i.isEmpty())
            return this;
        return new Interval(Math.min(lowerBound(), i.lowerBound()),
                             Math.max(upperBound(), i.upperBound()));
    }

    /**
     * Liefert den Durchschnitt dieses Intervalls und eines übergebenen Intervalls i.
     * @param i das andere Intervall
     * @return Durchschnitt (gemeinsame Punkte)
     */
    public Interval intersection(final Interval i) {
        if(isEmpty())
            return this;
        if(i.isEmpty())
            return i;
        return new Interval(Math.max(lowerBound(), i.lowerBound()),
                            Math.min(upperBound(), i.upperBound()));
    }

	public long getSize() {
		return size;
	}

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}

    /**
     * Testprogramm für Intervalle.
     * @param args nicht verwendet
     */
    /*
    public static void main(final String[]args) {
        final Interval a = new Interval(2, 5);
        final Interval b = new Interval(4, 6);
        final Interval c = new Interval(6, 9);
        final Interval d = new Interval();
        System.out.println(a.contains(d));
        System.out.println(d.contains(a));
        System.out.println(d.contains(d));
        System.out.println(a.hull(c).contains(b));
        System.out.println(a.intersection(c).isEmpty());
        System.out.println(a.intersection(b).contains(b));
    }*/
}