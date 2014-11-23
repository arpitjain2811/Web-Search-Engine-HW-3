package edu.nyu.cs.cs2580;

/**
 * Document with score.
 * 
 * @author fdiaz
 * @author congyu
 */
class ScoredDocument implements Comparable<ScoredDocument> {
  private Document _doc;
  private double _score;

  public ScoredDocument(Document doc, double score) {
    set_doc(doc);
    _score = score;
  }

  public String asTextResult() {
    StringBuffer buf = new StringBuffer();
    buf.append(get_doc()._docid).append("\t");
    buf.append(get_doc().getTitle()).append("\t");
    buf.append(_score).append("\t");
    buf.append(get_doc().getPageRank()).append("\t");
    buf.append(get_doc().getNumViews()).append("\t");
    return buf.toString();
  }

  /**
   * @CS2580: Student should implement {@code asHtmlResult} for final project.
   */
  public String asHtmlResult() {
    return "";
  }

  @Override
  public int compareTo(ScoredDocument o) {
    if (this._score == o._score) {
      return 0;
    }
    return (this._score > o._score) ? 1 : -1;
  }

  public Document get_doc() {
	 return _doc;
  }

  public void set_doc(Document _doc) {
	 this._doc = _doc;
  }

  public void updateScore(double additional){
    _score += additional;
  }
}
