
import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import DashboardLayout from '@/components/DashboardLayout';
import { ArrowUpRight, CreditCard, DollarSign, Users } from 'lucide-react';
import { ResponsiveContainer, AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip } from 'recharts';

const Dashboard = () => {
  const navigate = useNavigate();
  
  React.useEffect(() => {
    // Check if user is authenticated
    const token = localStorage.getItem('authToken');
    if (!token) {
      navigate('/');
    }
  }, [navigate]);

  // Mock data for dashboard stats
  const stats = [
    {
      title: "Total Spent",
      value: "$12,345.67",
      icon: <DollarSign className="h-4 w-4 text-muted-foreground" />,
      change: "+12% from last month"
    },
    {
      title: "Active Payment Methods",
      value: "3",
      icon: <CreditCard className="h-4 w-4 text-muted-foreground" />,
      change: "Iris, PIN, Fingerprint"
    },
    {
      title: "Merchants Used",
      value: "24",
      icon: <Users className="h-4 w-4 text-muted-foreground" />,
      change: "4 new this month"
    }
  ];
  
  // Mock data for the chart
  const chartData = [
    { name: 'Jan', amount: 400 },
    { name: 'Feb', amount: 300 },
    { name: 'Mar', amount: 600 },
    { name: 'Apr', amount: 800 },
    { name: 'May', amount: 500 },
    { name: 'Jun', amount: 900 },
    { name: 'Jul', amount: 1200 },
  ];
  
  // Mock recent transactions
  const recentTransactions = [
    { id: '123456', date: '2023-10-15', merchant: 'Coffee Shop', amount: '$4.50', status: 'Completed' },
    { id: '123455', date: '2023-10-14', merchant: 'Grocery Store', amount: '$65.27', status: 'Completed' },
    { id: '123454', date: '2023-10-12', merchant: 'Gas Station', amount: '$45.00', status: 'Completed' },
    { id: '123453', date: '2023-10-10', merchant: 'Online Store', amount: '$129.99', status: 'Processing' },
  ];

  return (
    <DashboardLayout>
      <div className="flex-1 space-y-4 p-4 md:p-8 pt-6">
        <div className="flex items-center justify-between">
          <h2 className="text-3xl font-bold tracking-tight">Dashboard</h2>
        </div>
        
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
          {stats.map((stat, index) => (
            <Card key={index}>
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium">
                  {stat.title}
                </CardTitle>
                {stat.icon}
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold">{stat.value}</div>
                <p className="text-xs text-muted-foreground flex items-center gap-1">
                  <ArrowUpRight className="h-4 w-4 text-emerald-500" />
                  {stat.change}
                </p>
              </CardContent>
            </Card>
          ))}
        </div>
        
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-7">
          <Card className="col-span-4">
            <CardHeader>
              <CardTitle>Monthly Spend Overview</CardTitle>
            </CardHeader>
            <CardContent className="pl-2">
              <ResponsiveContainer width="100%" height={350}>
                <AreaChart data={chartData}>
                  <defs>
                    <linearGradient id="colorAmount" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="5%" stopColor="#3b82f6" stopOpacity={0.8}/>
                      <stop offset="95%" stopColor="#3b82f6" stopOpacity={0}/>
                    </linearGradient>
                  </defs>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="name" />
                  <YAxis />
                  <Tooltip formatter={(value) => [`$${value}`, 'Amount']} />
                  <Area 
                    type="monotone" 
                    dataKey="amount" 
                    stroke="#3b82f6" 
                    fillOpacity={1} 
                    fill="url(#colorAmount)" 
                  />
                </AreaChart>
              </ResponsiveContainer>
            </CardContent>
          </Card>
          
          <Card className="col-span-3">
            <CardHeader>
              <CardTitle>Recent Transactions</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-3">
                {recentTransactions.map((transaction) => (
                  <div key={transaction.id} className="flex items-center justify-between border-b pb-2">
                    <div>
                      <p className="font-medium">{transaction.merchant}</p>
                      <p className="text-xs text-muted-foreground">{transaction.date}</p>
                    </div>
                    <div className="text-right">
                      <p className="font-medium">{transaction.amount}</p>
                      <p className={`text-xs ${transaction.status === 'Completed' ? 'text-green-600' : 'text-amber-600'}`}>
                        {transaction.status}
                      </p>
                    </div>
                  </div>
                ))}
              </div>
              <button 
                onClick={() => navigate('/transactions')}
                className="mt-4 text-sm text-blue-600 hover:text-blue-800 font-medium"
              >
                View all transactions &rarr;
              </button>
            </CardContent>
          </Card>
        </div>
      </div>
    </DashboardLayout>
  );
};

export default Dashboard;
