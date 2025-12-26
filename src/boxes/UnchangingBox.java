package boxes;

public class UnchangingBox extends Box {

    private final char unchangeableTopLetter;

    public UnchangingBox() {
        super();
        // Store the original top letter - it should never change visually
        this.unchangeableTopLetter = getTop();
    }

    @Override
    public char getTypeMarker() {
        return 'U';
    }

    /**
     * Override setTop to always maintain the original top letter.
     * Even if stamping tools try to change it, the top remains visually the same.
     */
    @Override
    public void setTop(char letter) {
        // Always set to the original unchangeable value
        super.setTop(unchangeableTopLetter);
    }
}