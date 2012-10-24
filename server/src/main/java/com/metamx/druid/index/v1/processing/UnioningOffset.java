package com.metamx.druid.index.v1.processing;

/**
 */
public class UnioningOffset implements Offset
{
  private final Offset[] offsets = new Offset[2];
  private final int[] offsetVals = new int[2];

  private int nextOffsetIndex;

  public UnioningOffset(
      Offset lhs,
      Offset rhs
  )
  {
    if (lhs.withinBounds()) {
      offsets[0] = lhs;
    }

    if (rhs.withinBounds()) {
      if (offsets[0] == null) {
        offsets[0] = rhs;
      }
      else {
        offsets[1] = rhs;
      }
    }

    if (offsets[0] != null) {
      offsetVals[0] = offsets[0].getOffset();
      if (offsets[1] != null) {
        offsetVals[1] = offsets[1].getOffset();
      }
    }
    figureOutNextValue();
  }

  private UnioningOffset(
      Offset[] offsets,
      int[] offsetVals,
      int nextOffsetIndex
  )
  {
    System.arraycopy(offsets, 0, this.offsets, 0, 2);
    System.arraycopy(offsetVals, 0, this.offsetVals, 0, 2);
    this.nextOffsetIndex = nextOffsetIndex;
  }

  private void figureOutNextValue() {
    if (offsets[0] != null) {
      if (offsets[1] != null) {
        int lhs = offsetVals[0];
        int rhs = offsetVals[1];

        if (lhs < rhs) {
          nextOffsetIndex = 0;
        } else if (lhs == rhs) {
          nextOffsetIndex = 0;
          rollIndexForward(1);
        }
        else {
          nextOffsetIndex = 1;
        }
      }
      else {
        nextOffsetIndex = 0;
      }
    }
  }

  private void rollIndexForward(int i) {
    offsets[i].increment();

    if (! offsets[i].withinBounds()) {
      offsets[i] = null;
      if (i == 0) {
        offsets[0] = offsets[1];
        offsetVals[0] = offsetVals[1];
      }
    }
    else {
      offsetVals[i] = offsets[i].getOffset();
    }
  }

  @Override
  public int getOffset()
  {
    return offsetVals[nextOffsetIndex];
  }

  @Override
  public void increment()
  {
    rollIndexForward(nextOffsetIndex);
    figureOutNextValue();
  }

  @Override
  public boolean withinBounds()
  {
    return offsets[0] != null;
  }

  @Override
  public Offset clone()
  {
    Offset[] newOffsets = new Offset[2];
    int[] newOffsetValues = new int[2];

    for(int i = 0; i < newOffsets.length; ++i) {
      newOffsets[i] = offsets[i] == null ? null : offsets[i].clone();
      newOffsetValues[i] = this.offsetVals[i];
    }

    return new UnioningOffset(newOffsets, newOffsetValues, nextOffsetIndex);
  }
}