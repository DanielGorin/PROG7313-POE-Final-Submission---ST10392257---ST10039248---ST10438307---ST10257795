package vcmsa.projects.pcv1.ui.expenses

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import vcmsa.projects.pcv1.R
import vcmsa.projects.pcv1.data.Expense
import java.text.SimpleDateFormat
import java.util.*

class ExpenseAdapter(private var expenses: List<Expense>) :
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

        // You’ll replace this with actual category name lookup
        holder.textCategory.text = "Category ID: ${expense.categoryId ?: "None"}"

        // Handle photo visibility
        if (!expense.photoUri.isNullOrEmpty()) {
            holder.imagePhoto.visibility = View.VISIBLE
            holder.imagePhoto.setImageURI(android.net.Uri.parse(expense.photoUri))
        } else {
            holder.imagePhoto.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = expenses.size

    fun updateData(newExpenses: List<Expense>) {
        expenses = newExpenses
        notifyDataSetChanged()
    }
}
