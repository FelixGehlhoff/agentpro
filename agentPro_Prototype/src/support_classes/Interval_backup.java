package support_classes;

/**
 * Definiert beschränktes, abgeschlossenes Intervall ganzer Zahlen.
 *
 * @author Klaus Köhler, koehler@hm.edu
 * @author Reinhard Schiedermeier, rs@cs.hm.edu
 * @version 15.06.2008
 */
public class Interval_backup {
    private final int lowerBound;
    private final int size;

    private final int upperBound;

    private final boolean isEmpty;

    /**
     * Erzeugt ein Intervall [l, u].
     * @param l untere Intervallgrenze
     * @param u obere Intervallgrenze
     */
    public Interval_backup(final int l, final int u) {
        this(l, u, u < l);
    }

    /**
     * Erzeugt ein leeres Intervall.
     */
    public Interval_backup() {
        this(0, 0, true);
    }

    public Interval_backup(final Interval_backup i) {
        this(i.lowerBound(), i.upperBound(), i.isEmpty());
    }

    /**
     * Erzeugt ein Intervall [l, u].
     * @param l untere Intervallgrenze
     * @param u obere Intervallgrenze
     * @param e wenn true: leeres Intervall
     */
    public Interval_backup(final int l, final int u, final boolean e) {
        lowerBound = l;
        upperBound = u;
        isEmpty = e;
        size = upperBound - lowerBound;
    }

    /**
     * Stellt fest, ob dieses Intervall leer ist.
     * @return true, wenn leer, sonst false
     */
    public boolean isEmpty() {
        return isEmpty;
    }

    public int lowerBound() {
        return lowerBound;
    }

    public int upperBound() {
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
        if(x.getClass() != Interval_backup.class)
            return false;
        final Interval_backup i = (Interval_backup)x;
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
        hash = 47*hash + lowerBound;
        hash = 47*hash + upperBound;
        return hash;
    }

    /**
     * Gibt Auskunft, ob die Zahl i in diesem Intervall enthalten ist.
     * @param i die Zahl
     * @return
     */
    public boolean contains(final int i) {
        if(isEmpty())
            return false;
        return i >= lowerBound()  &&  i <= upperBound();
    }

    /**
     * Gibt Auskunft, ob ein Intervall i Teilmenge dieses Intervalls ist.
     * @param i das zu vergleichende Intervall
     * @return true, wenn i Teilintervall ist, sonst false
     */
    public boolean contains(final Interval_backup i) {
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
    public boolean disjoint(final Interval_backup i) {
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
    public Interval_backup hull(final Interval_backup i) {
        if(isEmpty())
            return i;
        if(i.isEmpty())
            return this;
        return new Interval_backup(Math.min(lowerBound(), i.lowerBound()),
                             Math.max(upperBound(), i.upperBound()));
    }

    /**
     * Liefert den Durchschnitt dieses Intervalls und eines übergebenen Intervalls i.
     * @param i das andere Intervall
     * @return Durchschnitt (gemeinsame Punkte)
     */
    public Interval_backup intersection(final Interval_backup i) {
        if(isEmpty())
            return this;
        if(i.isEmpty())
            return i;
        return new Interval_backup(Math.max(lowerBound(), i.lowerBound()),
                            Math.min(upperBound(), i.upperBound()));
    }

	public int getSize() {
		return size;
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