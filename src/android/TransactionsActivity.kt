
package com.irispay.ui.transactions

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.irispay.R
import com.irispay.models.Transaction
import java.text.SimpleDateFormat
import java.util.*

/**
 * Activity displaying the user's transaction history with infinite scroll,
 * pull-to-refresh, and filters.
 */
class TransactionsActivity : AppCompatActivity() {
    
    private lateinit var viewModel: TransactionsViewModel
    private lateinit var adapter: TransactionsAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var emptyView: View
    private lateinit var loadingView: View
    private lateinit var errorView: View
    
    private var isLoading = false
    private var currentPage = 1
    private var hasMorePages = true
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transactions)
        
        setupViews()
        setupViewModel()
        setupRecyclerView()
        setupSwipeRefresh()
        
        loadInitialData()
    }
    
    private fun setupViews() {
        recyclerView = findViewById(R.id.transactions_recycler_view)
        swipeRefresh = findViewById(R.id.swipe_refresh)
        emptyView = findViewById(R.id.empty_view)
        loadingView = findViewById(R.id.loading_view)
        errorView = findViewById(R.id.error_view)
    }
    
    private fun setupViewModel() {
        viewModel = ViewModelProvider(this).get(TransactionsViewModel::class.java)
        
        viewModel.transactions.observe(this) { transactions ->
            if (transactions.isEmpty() && currentPage == 1) {
                showEmptyView()
            } else {
                showTransactions()
                adapter.submitList(transactions)
            }
        }
        
        viewModel.loading.observe(this) { loading ->
            isLoading = loading
            
            if (loading && currentPage == 1) {
                showLoadingView()
            } else {
                swipeRefresh.isRefreshing = loading && currentPage == 1
            }
        }
        
        viewModel.error.observe(this) { errorMessage ->
            errorMessage?.let {
                if (currentPage == 1) {
                    showErrorView()
                } else {
                    Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                }
            }
        }
        
        viewModel.hasMoreData.observe(this) { hasMore ->
            hasMorePages = hasMore
        }
    }
    
    private fun setupRecyclerView() {
        adapter = TransactionsAdapter()
        
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@TransactionsActivity)
            adapter = this@TransactionsActivity.adapter
            
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val firstVisibleItem = layoutManager.findFirstVisibleItemPosition()
                    
                    // Load more if we're near the end of the list
                    if (!isLoading && hasMorePages &&
                        (visibleItemCount + firstVisibleItem) >= totalItemCount - 5) {
                        loadMoreTransactions()
                    }
                }
            })
        }
    }
    
    private fun setupSwipeRefresh() {
        swipeRefresh.setOnRefreshListener {
            refreshTransactions()
        }
        
        // Set color scheme for the refresh indicator
        swipeRefresh.setColorSchemeResources(
            R.color.colorPrimary,
            R.color.colorAccent,
            R.color.colorPrimaryDark
        )
    }
    
    private fun loadInitialData() {
        currentPage = 1
        viewModel.loadTransactions(currentPage)
    }
    
    private fun loadMoreTransactions() {
        if (!isLoading && hasMorePages) {
            currentPage++
            viewModel.loadTransactions(currentPage)
        }
    }
    
    private fun refreshTransactions() {
        currentPage = 1
        viewModel.loadTransactions(currentPage, true)
    }
    
    private fun showLoadingView() {
        loadingView.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        emptyView.visibility = View.GONE
        errorView.visibility = View.GONE
    }
    
    private fun showTransactions() {
        loadingView.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
        emptyView.visibility = View.GONE
        errorView.visibility = View.GONE
    }
    
    private fun showEmptyView() {
        loadingView.visibility = View.GONE
        recyclerView.visibility = View.GONE
        emptyView.visibility = View.VISIBLE
        errorView.visibility = View.GONE
    }
    
    private fun showErrorView() {
        loadingView.visibility = View.GONE
        recyclerView.visibility = View.GONE
        emptyView.visibility = View.GONE
        errorView.visibility = View.VISIBLE
    }
}

/**
 * Adapter for the transactions RecyclerView.
 */
class TransactionsAdapter : RecyclerView.Adapter<TransactionViewHolder>() {
    private val transactions = mutableListOf<Transaction>()
    
    fun submitList(newTransactions: List<Transaction>) {
        if (newTransactions == transactions) return
        
        transactions.clear()
        transactions.addAll(newTransactions)
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(transactions[position])
    }
    
    override fun getItemCount(): Int = transactions.size
}

/**
 * ViewHolder for transaction items in the RecyclerView.
 */
class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val dateText: TextView = itemView.findViewById(R.id.transaction_date)
    private val merchantText: TextView = itemView.findViewById(R.id.transaction_merchant)
    private val amountText: TextView = itemView.findViewById(R.id.transaction_amount)
    private val statusText: TextView = itemView.findViewById(R.id.transaction_status)
    private val transactionIdText: TextView = itemView.findViewById(R.id.transaction_id)
    
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)
    
    fun bind(transaction: Transaction) {
        val date = transaction.date?.let {
            dateFormat.format(it)
        } ?: "Unknown date"
        
        dateText.text = date
        merchantText.text = transaction.merchant
        amountText.text = transaction.amount
        transactionIdText.text = transaction.id
        
        statusText.text = transaction.status.capitalize()
        
        // Set status color based on status value
        val statusColor = when (transaction.status.toLowerCase()) {
            "completed" -> R.color.status_completed
            "processing" -> R.color.status_processing
            "failed" -> R.color.status_failed
            else -> R.color.status_default
        }
        
        statusText.setTextColor(itemView.context.getColor(statusColor))
    }
}
