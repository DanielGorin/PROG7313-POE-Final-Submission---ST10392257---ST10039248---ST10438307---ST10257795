package vcmsa.projects.pcv1.ui.expenses

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import vcmsa.projects.pcv1.R
import vcmsa.projects.pcv1.data.Category
import vcmsa.projects.pcv1.data.Expense
import java.text.SimpleDateFormat
import java.util.*

class ExpenseAdapter(private var expenses: List<Expense>,
                     private var categoryMap: Map<Int, String>,
                     ) :
    RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    inner class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textAmount: TextView = itemView.findViewById(R.id.textAmount)
        val textCategory: TextView = itemView.findViewById(R.id.textCategory)
        val textDate: TextView = itemView.findViewById(R.id.textDate)
        val textTime: TextView = itemView.findViewById(R.id.textTime)
        val textDescription: TextView = itemView.findViewById(R.id.textDescription)
        val imagePhoto: ImageView = itemView.findViewById(R.id.imagePhoto)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_expense, parent, false)
        return ExpenseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val expense = expenses[position]

        holder.textAmount.text = "R%.2f".format(expense.amount)
        holder.textDescription.text = expense.description ?: ""

        // Format date and time from timestamp
        val date = Date(expense.timestamp)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        holder.textDate.text = dateFormat.format(date)
        holder.textTime.text = timeFormat.format(date)

        // Placeholder for actual category lookup
        val categoryName = expense.categoryId?.let { categoryMap[it] } ?: "Uncategorized"
        holder.textCategory.text = categoryName



        // Load photo using Glide
        if (!expense.photoUri.isNullOrEmpty()) {
            holder.imagePhoto.visibility = View.VISIBLE
            Glide.with(holder.itemView.context)
                .load(Uri.parse(expense.photoUri))
                .placeholder(R.drawable.placeholder) // optional
                .error(R.drawable.image_error)       // optional
                .into(holder.imagePhoto)
        } else {
            holder.imagePhoto.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = expenses.size

    fun updateData(newExpenses: List<Expense>, newCategoryMap: Map<Int, String>) {
        expenses = newExpenses
        categoryMap = newCategoryMap
        notifyDataSetChanged()
    }
}
