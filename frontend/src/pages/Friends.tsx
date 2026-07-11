import { useEffect, useState } from 'react';
import { friendsApi } from '../api/friends';
import { usersApi } from '../api/users';
import type { Friend, FriendshipRequestResponse, User } from '../types';
import toast from 'react-hot-toast';
import { format } from 'date-fns';
import {
  UserPlus,
  UserMinus,
  Check,
  X,
  Search,
  Users,
  Mail,
  Clock,
} from 'lucide-react';

export default function Friends() {
  const [friends, setFriends] = useState<Friend[]>([]);
  const [pendingRequests, setPendingRequests] = useState<FriendshipRequestResponse[]>([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState<User[]>([]);
  const [activeTab, setActiveTab] = useState<'friends' | 'requests' | 'search'>('friends');
  const [loading, setLoading] = useState(true);

  const loadData = async () => {
    try {
      const [friendsRes, pendingRes] = await Promise.all([
        friendsApi.getAllFriends(),
        friendsApi.getPendingRequests(),
      ]);
      setFriends(friendsRes.data);
      setPendingRequests(pendingRes.data);
    } catch (err) {
      toast.error('Failed to load friends');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const handleAccept = async (requestId: number) => {
    try {
      await friendsApi.acceptRequest(requestId);
      toast.success('Friend request accepted!');
      loadData();
    } catch (err: any) {
      toast.error(err.response?.data?.error || 'Failed to accept');
    }
  };

  const handleDecline = async (requestId: number) => {
    try {
      await friendsApi.declineRequest(requestId);
      toast.success('Friend request declined');
      loadData();
    } catch (err: any) {
      toast.error(err.response?.data?.error || 'Failed to decline');
    }
  };

  const handleRemove = async (friendshipId: number) => {
    try {
      await friendsApi.removeFriend(friendshipId);
      toast.success('Friend removed');
      loadData();
    } catch (err: any) {
      toast.error(err.response?.data?.error || 'Failed to remove');
    }
  };

  const handleSendRequest = async (userId: number) => {
    try {
      await friendsApi.sendRequest(userId);
      toast.success('Friend request sent!');
      setSearchResults([]);
      setSearchQuery('');
      loadData();
    } catch (err: any) {
      toast.error(err.response?.data?.error || 'Failed to send request');
    }
  };

  const handleSearch = async () => {
    if (!searchQuery.trim()) return;
    try {
      const res = await usersApi.getAll(0, 20);
      const filtered = res.data.content.filter(
        (u) =>
          u.username.toLowerCase().includes(searchQuery.toLowerCase()) ||
          u.name.toLowerCase().includes(searchQuery.toLowerCase())
      );
      setSearchResults(filtered);
    } catch (err) {
      toast.error('Search failed');
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600"></div>
      </div>
    );
  }

  return (
    <div className="space-y-4 md:space-y-6">
      <h1 className="text-xl md:text-2xl font-bold text-slate-900">Friends</h1>

      {/* Tabs */}
      <div className="flex gap-1 md:gap-2 bg-slate-100 p-1 rounded-lg w-full md:w-fit overflow-x-auto">
        {[
          { key: 'friends' as const, label: 'My Friends', count: friends.length },
          { key: 'requests' as const, label: 'Requests', count: pendingRequests.length },
          { key: 'search' as const, label: 'Find Friends', count: null },
        ].map((tab) => (
          <button
            key={tab.key}
            onClick={() => setActiveTab(tab.key)}
            className={`px-3 md:px-4 py-2 rounded-md text-xs md:text-sm font-medium transition-colors whitespace-nowrap ${
              activeTab === tab.key
                ? 'bg-white text-primary-600 shadow-sm'
                : 'text-slate-500 hover:text-slate-700'
            }`}
          >
            {tab.label}
            {tab.count !== null && tab.count > 0 && (
              <span className="ml-1 md:ml-2 bg-primary-100 text-primary-600 text-xs px-1.5 md:px-2 py-0.5 rounded-full">
                {tab.count}
              </span>
            )}
          </button>
        ))}
      </div>

      {/* Friends List */}
      {activeTab === 'friends' && (
        <div className="grid gap-2 md:gap-3">
          {friends.length === 0 ? (
            <div className="text-center py-8 md:py-12 bg-white rounded-xl border border-slate-200">
              <Users size={40} className="mx-auto text-slate-300 mb-3 md:w-12 md:h-12" />
              <p className="text-sm md:text-base text-slate-500">No friends yet. Try searching for people!</p>
            </div>
          ) : (
            friends.map((friend) => (
              <div
                key={friend.friendshipId}
                className="bg-white rounded-xl border border-slate-200 p-3 md:p-4 flex items-center justify-between"
              >
                <div className="flex items-center gap-3 md:gap-4">
                  <div className="w-8 h-8 md:w-10 md:h-10 bg-primary-100 text-primary-600 rounded-full flex items-center justify-center font-bold text-xs md:text-base">
                    {friend.username[0].toUpperCase()}
                  </div>
                  <div>
                    <p className="font-medium text-sm md:text-base text-slate-900">{friend.username}</p>
                    <p className="text-xs text-slate-400">{friend.name}</p>
                  </div>
                </div>
                <button
                  onClick={() => handleRemove(friend.friendshipId)}
                  className="flex items-center gap-1 px-2 md:px-3 py-1.5 md:py-2 text-xs md:text-sm text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                >
                  <UserMinus size={14} className="md:w-4 md:h-4" />
                  <span className="hidden sm:inline">Remove</span>
                </button>
              </div>
            ))
          )}
        </div>
      )}

      {/* Pending Requests */}
      {activeTab === 'requests' && (
        <div className="grid gap-2 md:gap-3">
          {pendingRequests.length === 0 ? (
            <div className="text-center py-8 md:py-12 bg-white rounded-xl border border-slate-200">
              <Mail size={40} className="mx-auto text-slate-300 mb-3 md:w-12 md:h-12" />
              <p className="text-sm md:text-base text-slate-500">No pending requests</p>
            </div>
          ) : (
            pendingRequests.map((req) => (
              <div
                key={req.id}
                className="bg-white rounded-xl border border-slate-200 p-3 md:p-4 flex flex-col sm:flex-row sm:items-center justify-between gap-3"
              >
                <div className="flex items-center gap-3 md:gap-4">
                  <div className="w-8 h-8 md:w-10 md:h-10 bg-amber-100 text-amber-600 rounded-full flex items-center justify-center font-bold text-xs md:text-base">
                    {req.requesterUsername[0].toUpperCase()}
                  </div>
                  <div>
                    <p className="font-medium text-sm md:text-base text-slate-900">{req.requesterUsername}</p>
                    <p className="text-xs text-slate-400">{req.requesterName}</p>
                    <p className="text-xs text-slate-400 flex items-center gap-1 mt-0.5">
                      <Clock size={12} />
                      {format(new Date(req.createdAt), 'MMM d, HH:mm')}
                    </p>
                  </div>
                </div>
                <div className="flex gap-2 ml-auto sm:ml-0">
                  <button
                    onClick={() => handleAccept(req.id)}
                    className="flex items-center gap-1 px-3 md:px-4 py-1.5 md:py-2 text-xs md:text-sm bg-emerald-500 text-white rounded-lg hover:bg-emerald-600 transition-colors"
                  >
                    <Check size={14} className="md:w-4 md:h-4" />
                    Accept
                  </button>
                  <button
                    onClick={() => handleDecline(req.id)}
                    className="flex items-center gap-1 px-3 md:px-4 py-1.5 md:py-2 text-xs md:text-sm bg-slate-200 text-slate-700 rounded-lg hover:bg-slate-300 transition-colors"
                  >
                    <X size={14} className="md:w-4 md:h-4" />
                    Decline
                  </button>
                </div>
              </div>
            ))
          )}
        </div>
      )}

      {/* Search */}
      {activeTab === 'search' && (
        <div className="space-y-4">
          <div className="flex gap-2">
            <input
              type="text"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
              placeholder="Search by username or name..."
              className="flex-1 px-3 md:px-4 py-2.5 md:py-3 border border-slate-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500 outline-none text-sm md:text-base"
            />
            <button
              onClick={handleSearch}
              className="flex items-center gap-1 md:gap-2 px-3 md:px-6 py-2.5 md:py-3 bg-primary-600 text-white rounded-lg hover:bg-primary-700 transition-colors text-sm md:text-base"
            >
              <Search size={16} className="md:w-[18px] md:h-[18px]" />
              <span className="hidden sm:inline">Search</span>
            </button>
          </div>

          <div className="grid gap-2 md:gap-3">
            {searchResults.map((u) => (
              <div
                key={u.id}
                className="bg-white rounded-xl border border-slate-200 p-3 md:p-4 flex flex-col sm:flex-row sm:items-center justify-between gap-3"
              >
                <div className="flex items-center gap-3 md:gap-4">
                  <div className="w-8 h-8 md:w-10 md:h-10 bg-primary-100 text-primary-600 rounded-full flex items-center justify-center font-bold text-xs md:text-base">
                    {u.username[0].toUpperCase()}
                  </div>
                  <div>
                    <p className="font-medium text-sm md:text-base text-slate-900">{u.username}</p>
                    <p className="text-xs text-slate-400">{u.name} · {u.email}</p>
                  </div>
                </div>
                <button
                  onClick={() => handleSendRequest(u.id)}
                  className="flex items-center gap-1 px-3 md:px-4 py-1.5 md:py-2 text-xs md:text-sm bg-primary-600 text-white rounded-lg hover:bg-primary-700 transition-colors self-end sm:self-auto"
                >
                  <UserPlus size={14} className="md:w-4 md:h-4" />
                  Add Friend
                </button>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
