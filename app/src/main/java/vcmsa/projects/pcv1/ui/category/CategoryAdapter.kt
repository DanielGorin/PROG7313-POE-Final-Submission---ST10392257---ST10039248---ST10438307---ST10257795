// Takudzwa Murwira – ST10392257, Jason Daniel Isaacs – ST10039248, Daniel Gorin – ST10438307 and Moegammad-Yaseen Salie – ST10257795
//PROG7313

//References:
//            https://medium.com/@SeanAT19/how-to-use-mpandroidchart-in-android-studio-c01a8150720f
//            https://chatgpt.com/
//            https://www.youtube.com/playlist?list=PLWz5rJ2EKKc8SmtMNw34wvYkqj45rV1d3
//            https://www.youtube.com/playlist?list=PLSrm9z4zp4mEPOfZNV9O-crOhoMa0G2-o
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

    // ViewHolder class
    inner class CategoryViewHolder(val binding: ItemCategoryBinding) :
        RecyclerView.ViewHolder(binding.root)

    // Inflate item layout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CategoryViewHolder(binding)
    }

    // Bind data to views
    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]
        with(holder.binding) {
            textCategoryName.text = category.name
            textCategoryIcon.text = category.icon ?: "📁"

            btnEdit.setOnClickListener { onEdit(category) }
            root.setOnClickListener { onClick(category) }
        }
    }

    override fun getItemCount() = categories.size

    // Update list data
    fun submitList(newList: List<Category>) {
        categories = newList
        notifyDataSetChanged()
    }

    // Get item by position
    fun getItemAt(position: Int): Category = categories[position]

}
