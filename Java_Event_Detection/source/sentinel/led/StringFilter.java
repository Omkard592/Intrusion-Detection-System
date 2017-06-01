/**
 * StringFilter.java --
 * Author          : Seokwon Yang
 * Created On      : Fri Jan  8 23:06:54 1999
 * Last Modified By: Seokwon Yang
 * Last Modified On: Fri Jan  8 23:06:56 1999
 * RCS             : $Id: header.el,v 1.1 1997/02/17 21:45:38 seyang Exp seyang $
 * Copyright (C) University of Florida 1999
 */

package sentinel.led;

class StringFilter implements Filter{
  protected String expression;
  protected Reactive context;


  /**
   * Constructor with an expression in 'String'
   *
   * @param expr a value of type 'String'
   */
  StringFilter(String expr) {
    this.expression = expr;
  }
  /**
   * Contructor with a context and an expression
   *
   * @param context a value of type 'Reactive'
   * @param expr a value of type 'String'
   */
  StringFilter(Reactive context, String expr) {
    this.expression = expr;
    this.context = context;
  }

  /**
   * check the expression with context. To call this function, StringFilter
   * should be created with 'StringFilter(Reactive context, String expr)'.
   * This function is usually called from 'FilterEvent.notify(Event)'.
   * @return a value of type 'boolean'
   */
  public boolean check() {
    if(context != null) return context.check(expression);
    else {
      System.out.println("Check if you create the filter with 'StringFilter(Reactive context, String expr)'");
      return false;
    }
  }

  /**
   * check the expression with a new reactive instance.
   *
   * @param reactive a value of type 'Reactive'
   * @return a value of type 'boolean'
   */
  public boolean check(Reactive reactive) {
    return reactive.check(expression);
  }
}

