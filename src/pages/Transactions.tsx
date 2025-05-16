
import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import DashboardLayout from '@/components/DashboardLayout';
import { 
  Table, 
  TableBody, 
  TableCell, 
  TableHead, 
  TableHeader, 
  TableRow 
} from '@/components/ui/table';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Badge } from '@/components/ui/badge';
import { Loader2, Search, Calendar } from 'lucide-react';
import { ResponsiveContainer, BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip } from 'recharts';

const Transactions = () => {
  const navigate = useNavigate();
  const [isLoading, setIsLoading] = useState(false);
  const [transactions, setTransactions] = useState(getMockTransactions());
  const [currentPage, setCurrentPage] = useState(1);
  const [statusFilter, setStatusFilter] = useState('all');
  const [dateFilter, setDateFilter] = useState({
    from: '',
    to: ''
  });

  // Helper for mock data generation
  function getMockTransactions() {
    const statuses = ['completed', 'processing', 'failed'];
    const merchants = ['Coffee Shop', 'Grocery Store', 'Gas Station', 'Online Store', 'Restaurant', 'Electronics Store'];
    
    // Generate 50 transactions
    return Array.from({ length: 50 }, (_, i) => {
      // Calculate a date within the last 90 days
      const date = new Date();
      date.setDate(date.getDate() - Math.floor(Math.random() * 90));
      
      const status = statuses[Math.floor(Math.random() * statuses.length)];
      const amount = (Math.random() * 200 + 5).toFixed(2);
      
      return {
        id: `TXN${(1000000 + i).toString()}`,
        date: date.toISOString().split('T')[0],
        merchant: merchants[Math.floor(Math.random() * merchants.length)],
        amount: `$${amount}`,
        status
      };
    }).sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime()); // Sort by date, newest first
  }

  React.useEffect(() => {
    // Check if user is authenticated
    const token = localStorage.getItem('authToken');
    if (!token) {
      navigate('/');
    }
  }, [navigate]);

  // Filter transactions based on current filters
  const filteredTransactions = transactions.filter(transaction => {
    // Filter by status
    if (statusFilter !== 'all' && transaction.status !== statusFilter) {
      return false;
    }
    
    // Filter by date range
    if (dateFilter.from && new Date(transaction.date) < new Date(dateFilter.from)) {
      return false;
    }
    
    if (dateFilter.to && new Date(transaction.date) > new Date(dateFilter.to)) {
      return false;
    }
    
    return true;
  });
  
  // Pagination
  const itemsPerPage = 10;
  const totalPages = Math.ceil(filteredTransactions.length / itemsPerPage);
  const paginatedTransactions = filteredTransactions.slice(
    (currentPage - 1) * itemsPerPage,
    currentPage * itemsPerPage
  );

  // Handle filter change
  const applyFilters = () => {
    setIsLoading(true);
    setCurrentPage(1); // Reset to first page when applying new filters
    
    // Simulate API call delay
    setTimeout(() => {
      setIsLoading(false);
    }, 500);
  };

  // Mock data for monthly transactions chart
  const chartData = [
    { month: 'Aug', amount: 1200 },
    { month: 'Sep', amount: 1800 },
    { month: 'Oct', amount: 1400 },
    { month: 'Nov', amount: 2200 },
  ];

  // Status badge color mapper
  const getStatusColor = (status) => {
    switch(status) {
      case 'completed':
        return 'bg-green-100 text-green-800';
      case 'processing':
        return 'bg-blue-100 text-blue-800';
      case 'failed':
        return 'bg-red-100 text-red-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };

  return (
    <DashboardLayout>
      <div className="flex-1 space-y-4 p-4 md:p-8 pt-6">
        <div className="flex items-center justify-between">
          <h2 className="text-3xl font-bold tracking-tight">Transactions</h2>
        </div>
        
        {/* Monthly spending chart */}
        <div className="p-4 bg-white rounded-lg border shadow-sm">
          <h3 className="text-lg font-medium mb-2">Monthly Spending</h3>
          <ResponsiveContainer width="100%" height={200}>
            <BarChart data={chartData}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="month" />
              <YAxis />
              <Tooltip 
                formatter={(value) => [`$${value}`, 'Amount']}
                labelStyle={{ color: '#333' }}
              />
              <Bar dataKey="amount" fill="#3b82f6" />
            </BarChart>
          </ResponsiveContainer>
        </div>
        
        {/* Filters */}
        <div className="grid gap-4 md:grid-cols-3 lg:grid-cols-4 mb-4">
          <div>
            <label className="text-sm font-medium mb-1 block">Status</label>
            <Select 
              value={statusFilter}
              onValueChange={(value) => setStatusFilter(value)}
            >
              <SelectTrigger>
                <SelectValue placeholder="Filter by status" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">All Statuses</SelectItem>
                <SelectItem value="completed">Completed</SelectItem>
                <SelectItem value="processing">Processing</SelectItem>
                <SelectItem value="failed">Failed</SelectItem>
              </SelectContent>
            </Select>
          </div>
          
          <div>
            <label className="text-sm font-medium mb-1 block">From Date</label>
            <div className="flex">
              <Input
                type="date"
                value={dateFilter.from}
                onChange={(e) => setDateFilter({...dateFilter, from: e.target.value})}
                className="w-full"
              />
            </div>
          </div>
          
          <div>
            <label className="text-sm font-medium mb-1 block">To Date</label>
            <div className="flex">
              <Input
                type="date"
                value={dateFilter.to}
                onChange={(e) => setDateFilter({...dateFilter, to: e.target.value})}
                className="w-full"
              />
            </div>
          </div>
          
          <div className="flex items-end">
            <Button onClick={applyFilters} disabled={isLoading} className="w-full">
              {isLoading ? <Loader2 className="mr-2 h-4 w-4 animate-spin" /> : <Search className="mr-2 h-4 w-4" />}
              Apply Filters
            </Button>
          </div>
        </div>
        
        {/* Transactions table */}
        <div className="bg-white rounded-lg border shadow-sm">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Date</TableHead>
                <TableHead>Transaction ID</TableHead>
                <TableHead>Merchant</TableHead>
                <TableHead>Amount</TableHead>
                <TableHead>Status</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {isLoading ? (
                <TableRow>
                  <TableCell colSpan={5} className="h-24 text-center">
                    <Loader2 className="mx-auto h-8 w-8 animate-spin text-muted-foreground" />
                  </TableCell>
                </TableRow>
              ) : paginatedTransactions.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={5} className="h-24 text-center">
                    No transactions found matching your filters.
                  </TableCell>
                </TableRow>
              ) : (
                paginatedTransactions.map((transaction) => (
                  <TableRow key={transaction.id}>
                    <TableCell>{transaction.date}</TableCell>
                    <TableCell className="font-mono">{transaction.id}</TableCell>
                    <TableCell>{transaction.merchant}</TableCell>
                    <TableCell>{transaction.amount}</TableCell>
                    <TableCell>
                      <Badge 
                        variant="outline" 
                        className={`capitalize ${getStatusColor(transaction.status)}`}
                      >
                        {transaction.status}
                      </Badge>
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
          
          {/* Pagination */}
          {totalPages > 1 && (
            <div className="flex items-center justify-between px-4 py-3 border-t">
              <div className="text-sm text-gray-500">
                Showing {((currentPage - 1) * itemsPerPage) + 1} to {Math.min(currentPage * itemsPerPage, filteredTransactions.length)} of {filteredTransactions.length} transactions
              </div>
              <div className="flex items-center gap-2">
                <Button 
                  variant="outline" 
                  size="sm"
                  disabled={currentPage === 1}
                  onClick={() => setCurrentPage(currentPage - 1)}
                >
                  Previous
                </Button>
                <Button
                  variant="outline"
                  size="sm"
                  disabled={currentPage === totalPages}
                  onClick={() => setCurrentPage(currentPage + 1)}
                >
                  Next
                </Button>
              </div>
            </div>
          )}
        </div>
      </div>
    </DashboardLayout>
  );
};

export default Transactions;
