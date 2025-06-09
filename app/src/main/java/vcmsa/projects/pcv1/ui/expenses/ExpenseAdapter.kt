package vcmsa.projects.pcv1.ui.expenses

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import vcmsa.projects.pcv1.R
import vcmsa.projects.pcv1.data.Expense
import vcmsa.projects.pcv1.databinding.ItemExpenseBinding
import java.text.SimpleDateFormat
import java.util.*

class ExpenseAdapter(
    private var expenses: List<Expense>,
    private var categoryMap: Map<Int, String>,
    private val onItemClick: (Expense) -> Unit
) : RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    val currentCategoryMap: Map<Int, String>
        get() = categoryMap
    /**
     * ViewHolder class that holds and binds the expense item views.
     * Responsible for populating each expense item UI with data.
     */
    inner class ExpenseViewHolder(private val binding: ItemExpenseBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(expense: Expense) {
            binding.apply {
                textAmount.text = "R%.2f".format(expense.amount)
                textDescription.text = expense.description.orEmpty()

                // Format timestamp into date and time
                val date = Date(expense.timestamp)
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

                textDate.text = dateFormat.format(date)
                textTime.text = timeFormat.format(date)

                textCategory.text = categoryMap[expense.categoryId] ?: "Uncategorized"

                if (!expense.photoUri.isNullOrEmpty()) {
                    imagePhoto.visibility = View.VISIBLE
                    Glide.with(root.context)
                        .load(Uri.parse(expense.photoUri))
                        .placeholder(R.drawable.placeholder)
                        .error(R.drawable.image_error)
                        .into(imagePhoto)
                } else {
                    imagePhoto.visibility = View.GONE
                }

                root.setOnClickListener {
                    onItemClick(expense)
                }
            }
        }
    }
    /**
     * Inflates the item layout and creates the ViewHolder instance.
     * Called when RecyclerView needs a new ViewHolder.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val binding = ItemExpenseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ExpenseViewHolder(binding)
    }
    /**
     * Binds the expense data at the given position to the ViewHolder.
     * Called by RecyclerView to display data in the ViewHolder.
     */
    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        holder.bind(expenses[position])
    }
    /**
     * Returns the total number of expense items.
     * Called by RecyclerView to determine how many items to display.
     */
    override fun getItemCount(): Int = expenses.size

    fun updateData(newExpenses: List<Expense>, newCategoryMap: Map<Int, String>) {
        expenses = newExpenses
        categoryMap = newCategoryMap
        notifyDataSetChanged()
    }
}
