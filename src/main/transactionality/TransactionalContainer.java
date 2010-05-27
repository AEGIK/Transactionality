package transactionality;

/** @author Christoffer Lerno */
abstract class TransactionalContainer<C> implements NonPrimitive
{
	private final Root m_root;
	private final ElementType<C> m_internalType;

	TransactionalContainer(Root root, ElementType<C> internalType)
	{
		m_root = root;
		m_internalType = internalType;
	}

	public Root root()
	{
		return m_root;
	}

	protected ElementType<C> internalType()
	{
		return m_internalType;
	}

	protected C createNewObject()
	{
		if (m_internalType == null) throw new UnsupportedOperationException("Object type not defined.");
		return m_internalType.newObject(root(), null);
	}

	protected void addUndo(Undo undo)
	{
		if (m_root == null) return;
		m_root.addAction(undo);
	}
}
