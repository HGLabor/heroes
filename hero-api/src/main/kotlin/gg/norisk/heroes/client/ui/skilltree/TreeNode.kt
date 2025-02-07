package gg.norisk.heroes.client.ui.skilltree

class TreeNode<T>(val value: T) {
    val children: MutableList<TreeNode<T>> = mutableListOf()

    fun addChild(child: TreeNode<T>): TreeNode<T> {
        children.add(child)
        return this
    }

    // Tiefensuche (Depth-First Search)
    fun dfs(visit: (T) -> Unit) {
        visit(value)
        children.forEach { it.dfs(visit) }
    }

    // Breitensuche (Breadth-First Search)
    fun bfs(visit: (T) -> Unit) {
        val queue = ArrayDeque<TreeNode<T>>()
        queue.add(this)

        while (queue.isNotEmpty()) {
            val currentNode = queue.removeFirst()
            visit(currentNode.value)
            currentNode.children.forEach { queue.add(it) }
        }
    }
}