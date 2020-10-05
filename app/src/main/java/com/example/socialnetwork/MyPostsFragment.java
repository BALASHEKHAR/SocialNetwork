package com.example.socialnetwork;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MyPostsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MyPostsFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public MyPostsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MyPostsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MyPostsFragment newInstance(String param1, String param2) {
        MyPostsFragment fragment = new MyPostsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }


    RecyclerView my_post_list;
    String currentUserId;
    DatabaseReference postRef;
    DatabaseReference likeRef;
    DatabaseReference userRef;
    Boolean likeChecker=false,editChecker=false;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v= inflater.inflate(R.layout.fragment_my_post, container, false);

        currentUserId= FirebaseAuth.getInstance().getCurrentUser().getUid();
        postRef= FirebaseDatabase.getInstance().getReference().child("Posts");
        userRef= FirebaseDatabase.getInstance().getReference().child("Users");
        likeRef= FirebaseDatabase.getInstance().getReference().child("Likes");
        my_post_list=v.findViewById(R.id.llllllo);
        my_post_list.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(getActivity());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        my_post_list.setLayoutManager(linearLayoutManager);
        displayMyPosts();
        return v;
    }

    private void displayMyPosts() {
        Query sortMyPost=  postRef.orderByChild("uid").startAt(currentUserId).endAt(currentUserId+"\uf8ff");

        FirebaseRecyclerOptions<Post> options=new FirebaseRecyclerOptions.Builder<Post>().setQuery(sortMyPost,Post.class).build();
        FirebaseRecyclerAdapter<Post, MyPostViewHolder> firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<Post, MyPostViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final MyPostViewHolder holder, final int position, @NonNull final Post model) {
                final String postKey=getRef(position).getKey();

                holder.setFullName(model.getFullName());
                holder.setTime(model.getTime());
                holder.setDate(model.getDate());
                holder.setDescription(model.getDescription());
                holder.setProfileImage(getActivity(),model.getProfileImage());
                holder.setPostImage(getActivity(),model.getPostImage());
                holder.setLikes(postKey);
                holder.likes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        likeChecker=true;
                        likeRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                if(likeChecker.equals(true))
                                {
                                    if(snapshot.child(postKey).hasChild(currentUserId))
                                    {
                                        likeChecker=false;
                                        likeRef.child(postKey).child(currentUserId).removeValue();

                                    }
                                    else
                                    {
                                        likeChecker=false;
                                        likeRef.child(postKey).child(currentUserId).setValue(true);
                                    }
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                    }
                });
                holder.comment.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent commentsIntnent=new Intent(getActivity(),ImageViewActivity.class);
                        commentsIntnent.putExtra("postKey",postKey);
                        commentsIntnent.putExtra("comment","false");
                        commentsIntnent.putExtra("uid",model.getUid());
                        startActivity(commentsIntnent);

                    }
                });
                holder.more.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        editChecker=true;
                        FirebaseDatabase.getInstance().getReference().child("Posts").child(postKey).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(editChecker.equals(true))
                                {
                                    editChecker=false;
                                    if(snapshot.exists())
                                    {
                                        final String  description=snapshot.child("description").getValue().toString();
                                        String  image=snapshot.child("postImage").getValue().toString();
                                        holder.setDescription(description);
                                        holder.setPostImage(holder.itemView.getContext(),image);
                                        String  databaseUserId=snapshot.child("uid").getValue().toString();
                                        CharSequence options[];
                                        if(databaseUserId.equals(currentUserId))
                                        {
                                            options=new CharSequence[]{
                                                    "Edit Post",
                                                    "Delete Post",
                                                    "comment Post",
                                                    "like Post"};
                                            AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
                                            builder.setTitle("select options");
                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, final int which) {
                                                    switch (which)
                                                    {
                                                        case 0:
                                                            holder.editCurrentPost(description,postKey);
                                                            break;
                                                        case 1:
                                                            getRef(position).removeValue();
                                                            break;
                                                        case 2:
                                                            Intent commentsIntnent=new Intent(getActivity(),ImageViewActivity.class);
                                                            commentsIntnent.putExtra("postKey",postKey);
                                                            commentsIntnent.putExtra("comment","false");
                                                            commentsIntnent.putExtra("uid",model.getUid());
                                                            startActivity(commentsIntnent);
                                                            break;
                                                        case 3:
                                                            likeChecker=true;
                                                            likeRef.addValueEventListener(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(@NonNull DataSnapshot snapshot) {

                                                                    if(likeChecker.equals(true))
                                                                    {
                                                                        if(snapshot.child(postKey).hasChild(currentUserId))
                                                                        {
                                                                            likeChecker=false;
                                                                            likeRef.child(postKey).child(currentUserId).removeValue();

                                                                        }
                                                                        else
                                                                        {
                                                                            likeChecker=false;
                                                                            likeRef.child(postKey).child(currentUserId).setValue(true);
                                                                        }
                                                                    }
                                                                }
                                                                @Override
                                                                public void onCancelled(@NonNull DatabaseError error) {

                                                                }
                                                            });
                                                            break;
                                                        default:
                                                    }
                                                }
                                            });
                                            builder.show();
                                        }
                                        else
                                        {
                                            options=new CharSequence[]{
                                                    "show Profile",
                                                    "comment Post",
                                                    "like Post"};
                                            AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
                                            builder.setTitle("select options");
                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    switch (which)
                                                    {

                                                        case 0:
                                                            String visit_user_id=model.getUid();
                                                            Intent PersonI=new Intent(getActivity(),PersonActivity.class);
                                                            PersonI.putExtra("visit_user_id",visit_user_id);
                                                            startActivity(PersonI);
                                                            break;
                                                        case 1:
                                                            Intent commentsIntnent=new Intent(getActivity(),ImageViewActivity.class);
                                                            commentsIntnent.putExtra("postKey",postKey);
                                                            commentsIntnent.putExtra("comment","false");
                                                            commentsIntnent.putExtra("uid",model.getUid());
                                                            startActivity(commentsIntnent);
                                                            break;
                                                        case 2:
                                                            likeChecker=true;
                                                            likeRef.addValueEventListener(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(@NonNull DataSnapshot snapshot) {

                                                                    if(likeChecker.equals(true))
                                                                    {
                                                                        if(snapshot.child(postKey).hasChild(currentUserId))
                                                                        {
                                                                            likeChecker=false;
                                                                            likeRef.child(postKey).child(currentUserId).removeValue();

                                                                        }
                                                                        else
                                                                        {
                                                                            likeChecker=false;
                                                                            likeRef.child(postKey).child(currentUserId).setValue(true);
                                                                        }
                                                                    }
                                                                }
                                                                @Override
                                                                public void onCancelled(@NonNull DatabaseError error) {

                                                                }
                                                            });
                                                            break;
                                                        default:
                                                    }
                                                }
                                            });
                                            builder.show();
                                        }
                                    }
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                    }
                });





            }

            @NonNull
            @Override
            public MyPostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v=LayoutInflater.from(getActivity()).inflate(R.layout.all_post_layout,parent,false);
                return  new MyPostViewHolder(v);

            }
        };
        my_post_list.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();

    }
    public static class MyPostViewHolder extends RecyclerView.ViewHolder{

        View mView;
        TextView no_of_likes;
        ImageButton likes,comment;
        int count_likes;
        DatabaseReference LikesRef;
        String currentUserId;
        ImageButton more;
        public MyPostViewHolder(@NonNull View itemView) {
            super(itemView);
            mView=itemView;
            no_of_likes=mView.findViewById(R.id.no_of_likes);
            likes=mView.findViewById(R.id.like);
            comment=mView.findViewById(R.id.comments);
            more=mView.findViewById(R.id.moreBtn);
            LikesRef= FirebaseDatabase.getInstance().getReference().child("Likes");
            currentUserId=FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
        public void setFullName(String fullName) {
            TextView userName=mView.findViewById(R.id.post_user_name);
            userName.setText(fullName);
        }
        public void setProfileImage(Context ctx, String profileImage) {
            CircleImageView image=mView.findViewById(R.id.post_profile_image);
            Picasso.with(ctx).load(profileImage).into(image);

        }
        public void setTime(String time) {
            TextView ptime=mView.findViewById(R.id.post_time);
            ptime.setText("   "+time);
        }
        public void setDate(String date) {
            TextView pdate=mView.findViewById(R.id.post_date);
            pdate.setText(date);
        }
        public void setDescription(String description) {
            TextView pdis=mView.findViewById(R.id.click_post_description);
            pdis.setText(description);
        }
        public void setPostImage(Context ctx,String postImage) {
            ImageView image=mView.findViewById(R.id.click_post_image);
            Picasso.with(ctx).load(postImage).into(image);

        }
        public void setLikes(final  String PostKey) {

            LikesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {


                    if(snapshot.child(PostKey).hasChild(currentUserId))
                    {

                        count_likes = (int) snapshot.child(PostKey).getChildrenCount();
                        likes.setImageResource(R.drawable.like);
                        no_of_likes.setText(count_likes +" Likes");
                    }
                    else
                    {

                        count_likes = (int) snapshot.child(PostKey).getChildrenCount();
                        likes.setImageResource(R.drawable.dislike);
                        no_of_likes.setText(count_likes +" Likes");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }
        private void editCurrentPost(String description,final String postKey) {
            androidx.appcompat.app.AlertDialog.Builder builder=new androidx.appcompat.app.AlertDialog.Builder(itemView.getContext());
            builder.setTitle("Edit Post");
            final EditText inputField=new EditText(itemView.getContext());
            inputField.setText(description);
            builder.setView(inputField);
            builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    FirebaseDatabase.getInstance().getReference().child("Posts").child(postKey).child("description").setValue(inputField.getText().toString());
                    Toast.makeText(itemView.getContext(),"Updated ",Toast.LENGTH_LONG).show();

                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();

                }
            });
            Dialog dialog=builder.create();
            dialog.show();
        }

    }

}