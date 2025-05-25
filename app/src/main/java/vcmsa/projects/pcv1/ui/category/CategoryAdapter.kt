package vcmsa.projects.pcv1.ui.category

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import vcmsa.projects.pcv1.databinding.ItemCategoryBinding
import vcmsa.projects.pcv1.data.Category

class CategoryAdapter(
    private val onEdit: (Category) -> Unit,
    private val onDelete: (Category) -> Unit,
    private val onClick: (Category) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    private var categories = listOf<Category>()

    inner class CategoryViewHolder(val binding: ItemCategoryBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]
        with(holder.binding) {
            textCategoryName.text = category.name
            textCategoryIcon.text = category.icon ?: "📁"

            btnEdit.setOnClickListener { onEdit(category) }
            btnDelete.setOnClickListener { onDelete(category) }
            root.setOnClickListener { onClick(category) }
        }
    }

    override fun getItemCount() = categories.size

    fun submitList(newList: List<Category>) {
        categories = newList
        notifyDataSetChanged()
    }

    fun getItemAt(position: Int): Category {
        return categories[position]
    }

}
