package org.aegik.transactionality;

import java.util.*;

/**
 * A tree root containing a single dictionary.
 * <p>
 * The root is used for starting transactions on a tree.
 * <p>
 * @see #beginTransaction()
 * @see #commit()
 * @see #rollback()
 *
 * @author Christoffer Lerno
 */
class Root
{
	private boolean m_transaction;
	private final LinkedList<Undo> m_undos;

	/**
	 * Creates a new root.
	 */
	public Root()
	{
		m_transaction  = false;
		m_undos = new LinkedList<Undo>();
	}

	/**
	 * Begins a transaction on this root.
	 * <p>
	 * Example:
	 * <code>
	 * <pre>
	 * root.beginTransaction();
	 * boolean rollback = true;
	 * try
	 * {
	 *
	 *    ... // Code changing the data contained in the root.
	 *
	 *    rollback = false;
	 *    root.commit();
	 * }
	 * finally
	 * {
	 *    // the rollback flag will be true if we failed to complete
	 *    // all actions in the try { ... } block.
	 *    if (rollback) root.rollback();
	 * }
	 * </pre>
	 * </code>
	 * @throws IllegalStateException if a transaction already was started.
	 */
	public void beginTransaction()
	{
		if (m_transaction) throw new IllegalStateException("Nested transactions not supported");
		m_transaction = true;
	}

	/**
	 * Commits a transaction.
	 * <p>
	 * This will end the transaction and remove all rollback information.
	 *
	 * @throws IllegalStateException if we're not currently in a transaction.
	 * @see #beginTransaction()
	 * @see #rollback()
	 */
	public void commit()
	{
		if (!m_transaction) throw new IllegalStateException("Not in transaction");
		m_transaction = false;
		m_undos.clear();
	}

	/**
	 * Rollbacks a transaction.
	 * <p>
	 * This will end the transaction by rolling back all changes since
	 * the transaction was started.
	 *
	 * @throws IllegalStateException if a transaction isn't active.
	 * @see #beginTransaction()
	 * @see #beginTransaction()
	 */
	public void rollback()
	{
		if (!m_transaction) throw new IllegalStateException("Not in transaction");
		m_transaction = false;
		while (m_undos.size() > 0)
		{
			m_undos.removeLast().undo();
		}
	}

	/**
	 * Tests if we are currently in a transaction.
	 *
	 * @return true if we are in a transaction, false otherwise.
	 */
	public boolean isInTransaction()
	{
		return m_transaction;
	}

	/**
	 * Adds an undo action for the root rollback if a transaction is active.
	 *
	 * @param undo the undo to run when rolling back.
	 */
	void addAction(Undo undo)
	{
		if (m_transaction)
		{
			m_undos.addLast(undo);
		}
	}
}
