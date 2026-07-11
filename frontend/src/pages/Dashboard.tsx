import { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { friendsApi } from '../api/friends';
import { availabilityApi } from '../api/availability';
import type { Friend, CommonSlot, FriendshipRequestResponse } from '../types';
import { Users, Calendar, Clock, UserCheck } from 'lucide-react';
import { format } from 'date-fns';

export default function Dashboard() {
  const { user } = useAuth();
  const [friends, setFriends] = useState<Friend[]>([]);
  const [pendingRequests, setPendingRequests] = useState<FriendshipRequestResponse[]>([]);
  const [commonSlots, setCommonSlots] = useState<CommonSlot[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const loadData = async () => {
      try {
        const [friendsRes, pendingRes, slotsRes] = await Promise.allSettled([
          friendsApi.getAllFriends(),
          friendsApi.getPendingRequests(),
          availabilityApi.getCommonSlots(),
        ]);

        if (friendsRes.status === 'fulfilled') setFriends(friendsRes.value.data);
        if (pendingRes.status === 'fulfilled') setPendingRequests(pendingRes.value.data);
        if (slotsRes.status === 'fulfilled') setCommonSlots(slotsRes.value.data);
      } catch (err) {
        console.error('Failed to load dashboard data', err);
      } finally {
        setLoading(false);
      }
    };
    loadData();
  }, []);

  const stats = [
    { label: 'Friends', value: friends.length, icon: Users, color: 'bg-blue-500' },
    { label: 'Pending Requests', value: pendingRequests.length, icon: UserCheck, color: 'bg-amber-500' },
    { label: 'Common Slots', value: commonSlots.length, icon: Clock, color: 'bg-emerald-500' },
  ];

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600"></div>
      </div>
    );
  }

  return (
    <div className="space-y-6 md:space-y-8">
      {/* Header */}
      <div>
        <h1 className="text-xl md:text-2xl font-bold text-slate-900">
          Welcome back, {user?.name || user?.username}! 👋
        </h1>
        <p className="text-sm md:text-base text-slate-500 mt-1">Here's what's happening with your social schedule</p>
      </div>

      {/* Stats Grid */}
      <div className="grid grid-cols-3 gap-3 md:gap-6">
        {stats.map((stat) => (
          <div
            key={stat.label}
            className="bg-white rounded-xl shadow-sm border border-slate-200 p-3 md:p-6 flex flex-col md:flex-row items-center md:items-center gap-2 md:gap-4"
          >
            <div className={`${stat.color} p-2 md:p-3 rounded-lg`}>
              <stat.icon size={20} className="text-white md:w-6 md:h-6" />
            </div>
            <div className="text-center md:text-left">
              <p className="text-xl md:text-2xl font-bold text-slate-900">{stat.value}</p>
              <p className="text-xs md:text-sm text-slate-500">{stat.label}</p>
            </div>
          </div>
        ))}
      </div>

      {/* Content Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-4 md:gap-6">
        {/* Pending Requests */}
        <div className="bg-white rounded-xl shadow-sm border border-slate-200 p-4 md:p-6">
          <h2 className="text-base md:text-lg font-semibold text-slate-900 mb-4 flex items-center gap-2">
            <UserCheck size={18} className="text-amber-500 md:w-5 md:h-5" />
            Friend Requests
          </h2>
          {pendingRequests.length === 0 ? (
            <p className="text-slate-400 text-sm">No pending requests</p>
          ) : (
            <div className="space-y-2 md:space-y-3">
              {pendingRequests.slice(0, 5).map((req) => (
                <div
                  key={req.id}
                  className="flex items-center justify-between p-2 md:p-3 bg-slate-50 rounded-lg"
                >
                  <div className="flex items-center gap-2 md:gap-3">
                    <div className="w-7 h-7 md:w-8 md:h-8 bg-primary-100 text-primary-600 rounded-full flex items-center justify-center text-xs font-bold">
                      {req.requesterUsername[0].toUpperCase()}
                    </div>
                    <div>
                      <p className="text-xs md:text-sm font-medium text-slate-700">
                        {req.requesterUsername}
                      </p>
                      <p className="text-xs text-slate-400">{req.requesterName}</p>
                    </div>
                  </div>
                  <span className="text-xs text-slate-400">
                    {format(new Date(req.createdAt), 'MMM d')}
                  </span>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Friends List */}
        <div className="bg-white rounded-xl shadow-sm border border-slate-200 p-4 md:p-6">
          <h2 className="text-base md:text-lg font-semibold text-slate-900 mb-4 flex items-center gap-2">
            <Users size={18} className="text-blue-500 md:w-5 md:h-5" />
            My Friends
          </h2>
          {friends.length === 0 ? (
            <p className="text-slate-400 text-sm">No friends yet. Try searching for people!</p>
          ) : (
            <div className="space-y-2 md:space-y-3">
              {friends.slice(0, 5).map((friend) => (
                <div
                  key={friend.friendshipId}
                  className="flex items-center gap-2 md:gap-3 p-2 md:p-3 bg-slate-50 rounded-lg"
                >
                  <div className="w-7 h-7 md:w-8 md:h-8 bg-primary-100 text-primary-600 rounded-full flex items-center justify-center text-xs font-bold">
                    {friend.username[0].toUpperCase()}
                  </div>
                  <div>
                    <p className="text-xs md:text-sm font-medium text-slate-700">{friend.username}</p>
                    <p className="text-xs text-slate-400">{friend.name}</p>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>

      {/* Common Slots */}
      {commonSlots.length > 0 && (
        <div className="bg-white rounded-xl shadow-sm border border-slate-200 p-4 md:p-6">
          <h2 className="text-base md:text-lg font-semibold text-slate-900 mb-4 flex items-center gap-2">
            <Clock size={18} className="text-emerald-500 md:w-5 md:h-5" />
            Common Free Time
          </h2>
          <div className="space-y-2 md:space-y-3">
            {commonSlots.slice(0, 5).map((slot, i) => (
              <div
                key={i}
                className="flex items-center justify-between p-2 md:p-3 bg-emerald-50 rounded-lg"
              >
                <div>
                  <p className="text-xs md:text-sm font-medium text-slate-700">
                    with {slot.friendUsername}
                  </p>
                  <p className="text-xs text-slate-500">
                    {format(new Date(slot.startTime), 'MMM d, HH:mm')} –{' '}
                    {format(new Date(slot.endTime), 'HH:mm')}
                  </p>
                </div>
                <Calendar size={14} className="text-emerald-500 md:w-4 md:h-4" />
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
