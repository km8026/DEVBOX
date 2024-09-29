import WriteLong from '../../components/WriteLong';
import WriteShort from '../../components/WriteShort';
import WriteSelect from '../../components/WriteSelect';
import Button from '../../components/Button';
import { useLocation, useNavigate } from 'react-router-dom';
import { useEffect, useState } from 'react';
import { useUser } from '../../components/context/UserContext';
import Swal from 'sweetalert2';
import InputScrollAndFocus from '../../components/InputScrollAndFocus';

const ReferenceUpdate = () => {
    const domain = "http://localhost:8080";

    const { user } = useUser();
    const navigate = useNavigate();

    const [title, setTitle] = useState('');
    const [selectJob, setSelectJob] = useState('');
    const [link, setLink] = useState('');
    const [content1, setContent1] = useState('');
    const [content2, setContent2] = useState('');
    const [content3, setContent3] = useState('');
    const [content4, setContent4] = useState('');
    const [content5, setContent5] = useState('');

    useEffect(() => {
        if (!user) {
            Swal.fire({
                icon: "error",
                title: "로그인이 필요합니다."
            }).then(() => {
                navigate('/auth');
            });
        }
    }, [user, navigate]);
    const token = localStorage.getItem('accessToken');

    const location = useLocation();
    const search = new URLSearchParams(location.search);
    const referenceId = search.get('referenceId');
    useEffect(() => {
        async function get() {
            const url = `${domain}/reference/update?referenceId=${referenceId}`;
            const res = await fetch(url, {
                credentials: 'include',
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });
            const data = await res.json();
            setTitle(data.title);
            setSelectJob(data.selectJob);
            setLink(data.link);
            setContent1(data.content1);
            setContent2(data.content2);
            setContent3(data.content3);
            setContent4(data.content4);
            setContent5(data.content5);
        }
        get();
    }, []);

    const validateFields = () => {
        if (!user) {
            Swal.fire({
                icon: "error",
                title: "로그인이 필요합니다."
            });
            return false;
        } else if (title.trim() === '') {
            InputScrollAndFocus("title", "제목을 입력해주세요.");
            setTitle('');
            return false;
        } else if (selectJob === '') {
            Swal.fire({
                icon: "warning",
                title: "카테고리를 선택해주세요.",
            });
            window.scrollTo(0, 0);
            return false;
        } else if (link.trim() === '') {
            InputScrollAndFocus("link", "사이트 주소를 입력해주세요.");
            setLink('');
            return false;
        } else if (content1.trim() === '') {
            InputScrollAndFocus("content1", "내용1을 입력해주세요.");
            setContent1('');
            return false;
        } else if (content2.trim() === '') {
            InputScrollAndFocus("content2", "내용2를 입력해주세요.");
            setContent2('');
            return false;
        }
        return true;
    };

    const updateData = async () => {
        try {
            const url = `${domain}/reference/update`;
            const response = await fetch(url, {
                method: 'POST',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify({
                    id: referenceId, title: title, selectJob: selectJob, link: link,
                    content1: content1, content2: content2, content3: content3.trim(), content4: content4.trim(), content5: content5.trim()
                })
            });
            if (!response.ok) {
                throw new Error("서버에서 오류가 발생했습니다.");
            }
            const data = await response.json();
            if (data.code === 200) {
                Swal.fire({
                    icon: "success",
                    title: "수정되었습니다."
                }).then(() => {
                    navigate('/reference/list');
                });
            } else {
                Swal.fire({
                    icon: "error",
                    title: "다시 입력해주세요."
                });
            }
        } catch (error) {
            Swal.fire({
                icon: "error",
                title: "수정 중 오류가 발생했습니다. 다시 시도해주세요."
            });
        }
    };

    const handleUpdate = async (e) => {
        e.preventDefault();
        if (validateFields()) {
            const result = await Swal.fire({
                title: "수정하시겠습니까?",
                icon: "warning",
                showCancelButton: true,
                confirmButtonText: "수정",
                confirmButtonColor: "#3085d6",
                cancelButtonText: "취소",
                cancelButtonColor: "#d33",
            });
            if (result.isConfirmed) {
                updateData();
            }
        }
    };

    return (
        <section className="container py-5">
            <div className="container py-5">
                <h1 className="h2 semi-bold-600 text-center mt-2">추천해요</h1>
                <p className="text-center pb-5 light-300">다른 사람에게 알려주고 싶은 나만의 꿀팁을 공유해요!</p>
                <div className="pricing-list rounded-top rounded-3 py-sm-0 py-5">
                    <div className="contact-form row">
                        <WriteShort type={'text'} titleTag={'제목'} name={'title'} value={title} onChange={(e) => { setTitle(e.target.value) }} />
                        <WriteSelect titleTag="카테고리" name="intro"
                            value={selectJob || "카테고리를 선택해주세요."} onChange={(e) => setSelectJob(e.target.value)}
                            options={["Web", "DevOps", "Cloud", "Data", "Mobile", "Others"]} />
                        <WriteLong titleTag={'사이트 주소'} name={'link'} value={link} onChange={(e) => { setLink(e.target.value) }} />
                        <WriteLong titleTag={'내용1 (필수)'} name={'content1'} value={content1} onChange={(e) => { setContent1(e.target.value) }} />
                        <WriteLong titleTag={'내용2 (필수)'} name={'content2'} value={content2} onChange={(e) => { setContent2(e.target.value) }} />
                        <WriteLong titleTag={'내용3 (선택)'} name={'content3'} value={content3} onChange={(e) => { setContent3(e.target.value) }} />
                        <WriteLong titleTag={'내용4 (선택)'} name={'content4'} value={content4} onChange={(e) => { setContent4(e.target.value) }} />
                        <WriteLong titleTag={'내용5 (선택)'} name={'content5'} value={content5} onChange={(e) => { setContent5(e.target.value) }} />
                    </div>
                </div>
            </div>
            <div className="form-row pt-2">
                <div className="col-md-12 col-10 text-end">
                    <Button text={'수정하기'} onClick={handleUpdate} />
                </div>
            </div>
        </section>
    );
};

export default ReferenceUpdate;